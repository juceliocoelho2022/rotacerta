package com.rotacerta.api.service;

import com.rotacerta.api.dto.InventoryDtos.InventoryItemResponse;
import com.rotacerta.api.dto.InventoryDtos.InventoryReservationRequest;
import com.rotacerta.api.dto.InventoryDtos.MovementResponse;
import com.rotacerta.api.dto.InventoryDtos.ProductCreateRequest;
import com.rotacerta.api.dto.InventoryDtos.ReservationResponse;
import com.rotacerta.api.dto.InventoryDtos.StockEntryRequest;
import com.rotacerta.api.model.Inventory;
import com.rotacerta.api.model.InventoryMovement;
import com.rotacerta.api.model.InventoryMovementType;
import com.rotacerta.api.model.InventoryReservation;
import com.rotacerta.api.model.InventoryReservationStatus;
import com.rotacerta.api.model.OrderEntity;
import com.rotacerta.api.model.OrderItem;
import com.rotacerta.api.model.Product;
import com.rotacerta.api.repository.InventoryMovementRepository;
import com.rotacerta.api.repository.InventoryRepository;
import com.rotacerta.api.repository.InventoryReservationRepository;
import com.rotacerta.api.repository.OrderRepository;
import com.rotacerta.api.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Service
public class InventoryService {
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryMovementRepository movementRepository;
    private final OrderRepository orderRepository;

    public InventoryService(ProductRepository productRepository,
                            InventoryRepository inventoryRepository,
                            InventoryReservationRepository reservationRepository,
                            InventoryMovementRepository movementRepository,
                            OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.movementRepository = movementRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> findAll() {
        return inventoryRepository.findAllWithProduct().stream().map(this::toInventoryResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> findLowStock() {
        return inventoryRepository.findLowStock().stream().map(this::toInventoryResponse).toList();
    }

    @Transactional
    public InventoryItemResponse createProduct(ProductCreateRequest request) {
        String sku = normalizeSku(request.sku());
        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new IllegalArgumentException("SKU já cadastrado: " + sku);
        }

        Product product = productRepository.save(new Product(
                sku,
                request.name().trim(),
                trimToNull(request.description()),
                request.unitPrice(),
                request.weightKg(),
                request.volumeM3()
        ));

        Inventory inventory = inventoryRepository.save(new Inventory(
                product,
                request.initialQuantity(),
                request.minimumQuantity(),
                trimToNull(request.warehouseLocation())
        ));

        if (request.initialQuantity() > 0) {
            movementRepository.save(new InventoryMovement(
                    product,
                    null,
                    InventoryMovementType.ENTRY,
                    request.initialQuantity(),
                    0,
                    request.initialQuantity(),
                    0,
                    0,
                    "Estoque inicial"
            ));
        }

        return toInventoryResponse(inventory);
    }

    @Transactional
    public InventoryItemResponse addStock(String sku, StockEntryRequest request) {
        Inventory inventory = getInventoryForUpdate(sku);
        int previousTotal = inventory.getTotalQuantity();
        int previousReserved = inventory.getReservedQuantity();

        inventory.addStock(request.quantity());
        inventoryRepository.save(inventory);
        saveMovement(inventory, null, InventoryMovementType.ENTRY, request.quantity(),
                previousTotal, previousReserved, trimToNull(request.reason()));
        return toInventoryResponse(inventory);
    }

    @Transactional
    public ReservationResponse reserve(Long orderId, InventoryReservationRequest request) {
        OrderEntity order = getOrder(orderId);
        return reserveInternal(order, request.sku(), request.quantity());
    }

    @Transactional
    public ReservationResponse release(Long orderId, String sku) {
        OrderEntity order = getOrder(orderId);
        Inventory inventory = getInventoryForUpdate(sku);
        InventoryReservation reservation = getReservation(orderId, inventory.getProduct().getId());
        releaseReservation(order, inventory, reservation);
        return toReservationResponse(reservation);
    }

    @Transactional
    public ReservationResponse pick(Long orderId, String sku) {
        OrderEntity order = getOrder(orderId);
        Inventory inventory = getInventoryForUpdate(sku);
        InventoryReservation reservation = getReservation(orderId, inventory.getProduct().getId());
        pickReservation(order, inventory, reservation, reservation.getQuantity());
        return toReservationResponse(reservation);
    }

    @Transactional
    public void reserveOrderItems(OrderEntity order, List<OrderItem> items) {
        for (Map.Entry<String, Integer> entry : aggregateSkuQuantities(items).entrySet()) {
            Inventory inventory = getInventoryForUpdate(entry.getKey());
            Product product = inventory.getProduct();
            InventoryReservation existing = reservationRepository
                    .findByOrderIdAndProductId(order.getId(), product.getId())
                    .orElse(null);

            if (existing == null) {
                reserveInternal(order, product.getSku(), entry.getValue());
                continue;
            }

            if (existing.getStatus() == InventoryReservationStatus.RESERVED
                    && existing.getQuantity() == entry.getValue()) {
                continue;
            }

            if (existing.getStatus() == InventoryReservationStatus.CONFIRMED) {
                throw new IllegalArgumentException("O picking do SKU " + product.getSku() + " já foi confirmado para o pedido.");
            }

            throw new IllegalArgumentException(
                    "A reserva do SKU " + product.getSku() + " não pode ser recriada após " + existing.getStatus() + "."
            );
        }
    }

    @Transactional
    public void pickOrderItems(OrderEntity order, List<OrderItem> items) {
        for (Map.Entry<String, Integer> entry : aggregateSkuQuantities(items).entrySet()) {
            Inventory inventory = getInventoryForUpdate(entry.getKey());
            InventoryReservation reservation = getReservation(order.getId(), inventory.getProduct().getId());

            if (reservation.getStatus() == InventoryReservationStatus.CONFIRMED
                    && reservation.getQuantity() == entry.getValue()) {
                continue;
            }

            pickReservation(order, inventory, reservation, entry.getValue());
        }
    }

    @Transactional
    public void releaseOrderReservations(OrderEntity order) {
        for (InventoryReservation reservation : reservationRepository.findByOrderIdOrderByCreatedAtAsc(order.getId())) {
            if (reservation.getStatus() != InventoryReservationStatus.RESERVED) {
                continue;
            }
            Inventory inventory = getInventoryForUpdate(reservation.getProduct().getSku());
            releaseReservation(order, inventory, reservation);
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findReservations(Long orderId) {
        getOrder(orderId);
        return reservationRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
                .map(this::toReservationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovementResponse> findMovements() {
        return movementRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .map(this::toMovementResponse)
                .toList();
    }

    private ReservationResponse reserveInternal(OrderEntity order, String sku, int quantity) {
        Inventory inventory = getInventoryForUpdate(sku);
        Product product = inventory.getProduct();

        InventoryReservation existing = reservationRepository
                .findByOrderIdAndProductId(order.getId(), product.getId())
                .orElse(null);
        if (existing != null && existing.getStatus() == InventoryReservationStatus.RESERVED) {
            throw new IllegalArgumentException("O pedido já possui reserva ativa para o SKU " + product.getSku());
        }
        if (existing != null && existing.getStatus() == InventoryReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException("O picking deste SKU já foi confirmado para o pedido.");
        }
        if (existing != null) {
            throw new IllegalArgumentException("Já existe histórico de reserva para este pedido e SKU. Use outro pedido para a nova operação.");
        }

        int previousTotal = inventory.getTotalQuantity();
        int previousReserved = inventory.getReservedQuantity();
        inventory.reserve(quantity);
        inventoryRepository.save(inventory);

        InventoryReservation reservation = reservationRepository.save(
                new InventoryReservation(order, product, quantity)
        );
        saveMovement(inventory, order, InventoryMovementType.RESERVATION, quantity,
                previousTotal, previousReserved, "Reserva de estoque para pedido " + order.getOrderNumber());
        return toReservationResponse(reservation);
    }

    private void releaseReservation(OrderEntity order, Inventory inventory, InventoryReservation reservation) {
        if (reservation.getStatus() != InventoryReservationStatus.RESERVED) {
            throw new IllegalArgumentException("A reserva não está ativa e não pode ser liberada.");
        }

        int previousTotal = inventory.getTotalQuantity();
        int previousReserved = inventory.getReservedQuantity();
        inventory.release(reservation.getQuantity());
        reservation.release();
        inventoryRepository.save(inventory);
        reservationRepository.save(reservation);
        saveMovement(inventory, order, InventoryMovementType.RESERVATION_RELEASE, reservation.getQuantity(),
                previousTotal, previousReserved, "Liberação de reserva do pedido " + order.getOrderNumber());
    }

    private void pickReservation(OrderEntity order, Inventory inventory, InventoryReservation reservation, int expectedQuantity) {
        if (reservation.getStatus() != InventoryReservationStatus.RESERVED) {
            throw new IllegalArgumentException("A reserva não está ativa e não pode ser processada no picking.");
        }
        if (reservation.getQuantity() != expectedQuantity) {
            throw new IllegalArgumentException(
                    "A quantidade reservada para o SKU " + inventory.getProduct().getSku()
                            + " diverge da quantidade atual do pedido."
            );
        }

        int previousTotal = inventory.getTotalQuantity();
        int previousReserved = inventory.getReservedQuantity();
        inventory.pick(reservation.getQuantity());
        reservation.confirm();
        inventoryRepository.save(inventory);
        reservationRepository.save(reservation);
        saveMovement(inventory, order, InventoryMovementType.PICKING, reservation.getQuantity(),
                previousTotal, previousReserved, "Picking confirmado para pedido " + order.getOrderNumber());
    }

    private Map<String, Integer> aggregateSkuQuantities(List<OrderItem> items) {
        Map<String, Integer> quantities = new TreeMap<>();
        if (items == null) {
            return quantities;
        }
        for (OrderItem item : items) {
            if (item.getSku() == null || item.getSku().isBlank()) {
                continue;
            }
            quantities.merge(normalizeSku(item.getSku()), item.getQuantity(), Integer::sum);
        }
        return quantities;
    }

    private void saveMovement(Inventory inventory, OrderEntity order, InventoryMovementType type, int quantity,
                              int previousTotal, int previousReserved, String reason) {
        movementRepository.save(new InventoryMovement(
                inventory.getProduct(), order, type, quantity,
                previousTotal, inventory.getTotalQuantity(),
                previousReserved, inventory.getReservedQuantity(), reason
        ));
    }

    private Inventory getInventoryForUpdate(String sku) {
        String normalized = normalizeSku(sku);
        return inventoryRepository.findBySkuForUpdate(normalized)
                .orElseThrow(() -> new IllegalArgumentException("SKU não encontrado no estoque: " + normalized));
    }

    private InventoryReservation getReservation(Long orderId, Long productId) {
        return reservationRepository.findByOrderIdAndProductId(orderId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada para o pedido e SKU informados."));
    }

    private OrderEntity getOrder(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("O ID do pedido é obrigatório.");
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado. ID: " + orderId));
    }

    private InventoryItemResponse toInventoryResponse(Inventory inventory) {
        Product product = inventory.getProduct();
        int available = inventory.getAvailableQuantity();
        String status = available == 0 ? "OUT_OF_STOCK" : available <= inventory.getMinimumQuantity() ? "LOW_STOCK" : "OK";
        return new InventoryItemResponse(
                inventory.getId(), product.getId(), product.getSku(), product.getName(), product.getUnitPrice(),
                product.getWeightKg(), product.getVolumeM3(),
                inventory.getTotalQuantity(), inventory.getReservedQuantity(), available,
                inventory.getMinimumQuantity(), inventory.getWarehouseLocation(), status, inventory.getUpdatedAt()
        );
    }

    private ReservationResponse toReservationResponse(InventoryReservation reservation) {
        return new ReservationResponse(
                reservation.getId(), reservation.getOrder().getId(), reservation.getOrder().getOrderNumber(),
                reservation.getProduct().getSku(), reservation.getProduct().getName(), reservation.getQuantity(),
                reservation.getStatus(), reservation.getCreatedAt(), reservation.getReleasedAt()
        );
    }

    private MovementResponse toMovementResponse(InventoryMovement movement) {
        OrderEntity order = movement.getOrder();
        return new MovementResponse(
                movement.getId(), movement.getProduct().getSku(), movement.getProduct().getName(),
                order != null ? order.getId() : null, order != null ? order.getOrderNumber() : null,
                movement.getMovementType(), movement.getQuantity(), movement.getPreviousTotal(), movement.getNewTotal(),
                movement.getPreviousReserved(), movement.getNewReserved(), movement.getReason(), movement.getCreatedAt()
        );
    }

    private String normalizeSku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("O SKU é obrigatório.");
        }
        return sku.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
