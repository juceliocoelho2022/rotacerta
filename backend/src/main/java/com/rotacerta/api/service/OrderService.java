package com.rotacerta.api.service;

import com.rotacerta.api.dto.DashboardResponse;
import com.rotacerta.api.dto.OrderCreateRequest;
import com.rotacerta.api.dto.OrderDeliveryDetailsResponse;
import com.rotacerta.api.dto.OrderDetailResponse;
import com.rotacerta.api.dto.OrderItemCreateRequest;
import com.rotacerta.api.dto.OrderItemResponse;
import com.rotacerta.api.dto.OrderSummaryResponse;
import com.rotacerta.api.dto.StatusUpdateRequest;
import com.rotacerta.api.dto.TrackingEventResponse;
import com.rotacerta.api.dto.TrackingResponse;
import com.rotacerta.api.model.Customer;
import com.rotacerta.api.model.CustomerAddress;
import com.rotacerta.api.model.DeliveryLocation;
import com.rotacerta.api.model.DeliveryPreference;
import com.rotacerta.api.model.DeliveryStatus;
import com.rotacerta.api.model.DeliveryType;
import com.rotacerta.api.model.OrderDeliveryDetails;
import com.rotacerta.api.model.OrderEntity;
import com.rotacerta.api.model.OrderItem;
import com.rotacerta.api.model.OrderPriority;
import com.rotacerta.api.model.Product;
import com.rotacerta.api.model.TrackingEvent;
import com.rotacerta.api.repository.CustomerAddressRepository;
import com.rotacerta.api.repository.CustomerRepository;
import com.rotacerta.api.repository.DeliveryLocationRepository;
import com.rotacerta.api.repository.DeliveryPreferenceRepository;
import com.rotacerta.api.repository.OrderDeliveryDetailsRepository;
import com.rotacerta.api.repository.OrderItemRepository;
import com.rotacerta.api.repository.OrderRepository;
import com.rotacerta.api.repository.ProductRepository;
import com.rotacerta.api.repository.TrackingEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final ZoneId OPERATIONS_ZONE = ZoneId.of("America/Sao_Paulo");

    private final OrderRepository orderRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final LiveTrackingService liveTrackingService;
    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final DeliveryPreferenceRepository deliveryPreferenceRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderDeliveryDetailsRepository orderDeliveryDetailsRepository;
    private final DeliveryLocationRepository deliveryLocationRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public OrderService(
            OrderRepository orderRepository,
            TrackingEventRepository trackingEventRepository,
            LiveTrackingService liveTrackingService,
            CustomerRepository customerRepository,
            CustomerAddressRepository customerAddressRepository,
            DeliveryPreferenceRepository deliveryPreferenceRepository,
            OrderItemRepository orderItemRepository,
            OrderDeliveryDetailsRepository orderDeliveryDetailsRepository,
            DeliveryLocationRepository deliveryLocationRepository,
            ProductRepository productRepository,
            InventoryService inventoryService
    ) {
        this.orderRepository = orderRepository;
        this.trackingEventRepository = trackingEventRepository;
        this.liveTrackingService = liveTrackingService;
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.deliveryPreferenceRepository = deliveryPreferenceRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderDeliveryDetailsRepository = orderDeliveryDetailsRepository;
        this.deliveryLocationRepository = deliveryLocationRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> findAll() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public OrderSummaryResponse findById(Long id) {
        return toSummary(getOrder(id));
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse findDetailById(Long id) {
        OrderEntity order = getOrder(id);
        List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());
        OrderDeliveryDetails delivery = orderDeliveryDetailsRepository.findByOrderId(order.getId()).orElse(null);
        return toDetail(order, items, delivery);
    }

    @Transactional
    public OrderDetailResponse create(OrderCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Os dados do pedido são obrigatórios.");
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado. ID: " + request.customerId()));

        CustomerAddress address = customerAddressRepository.findById(request.addressId())
                .orElseThrow(() -> new IllegalArgumentException("Endereço não encontrado. ID: " + request.addressId()));

        if (address.getCustomer() == null || !customer.getId().equals(address.getCustomer().getId())) {
            throw new IllegalArgumentException("O endereço selecionado não pertence ao cliente informado.");
        }

        validateDeliveryDate(request.deliveryDate(), request.deliveryType());

        DeliveryPreference preference = deliveryPreferenceRepository.findByCustomerId(customer.getId()).orElse(null);
        LocalTime windowStart = request.windowStart();
        LocalTime windowEnd = request.windowEnd();

        if (windowStart == null && windowEnd == null && preference != null) {
            windowStart = preference.getPreferredStartTime();
            windowEnd = preference.getPreferredEndTime();
        }

        validateTimeWindow(windowStart, windowEnd, request.deliveryType());

        List<Product> products = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemCreateRequest item : request.items()) {
            Product product = getActiveProduct(item.sku());
            products.add(product);
            total = total.add(product.getUnitPrice().multiply(BigDecimal.valueOf(item.quantity())));
        }
        total = total.setScale(2, RoundingMode.HALF_UP);

        OrderEntity order = orderRepository.save(new OrderEntity(
                generateOrderNumber(),
                customer,
                total,
                DeliveryStatus.ORDER_CREATED,
                request.priority(),
                request.deliveryType(),
                generateTrackingCode()
        ));

        List<OrderItem> items = new ArrayList<>();
        for (int index = 0; index < request.items().size(); index++) {
            items.add(toEntity(order, request.items().get(index), products.get(index)));
        }
        List<OrderItem> savedItems = orderItemRepository.saveAll(items);

        String instructions = preference != null ? preference.getDeliveryInstructions() : null;
        OrderDeliveryDetails delivery = orderDeliveryDetailsRepository.save(new OrderDeliveryDetails(
                order,
                address,
                request.deliveryDate(),
                windowStart,
                windowEnd,
                instructions
        ));

        syncDispatchLocation(order, delivery);

        trackingEventRepository.save(new TrackingEvent(
                order,
                DeliveryStatus.ORDER_CREATED,
                address.getCity() + "/" + address.getState(),
                OffsetDateTime.now()
        ));

        return toDetail(order, savedItems, delivery);
    }

    @Transactional(readOnly = true)
    public TrackingResponse track(String trackingCode) {
        OrderEntity order = orderRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new IllegalArgumentException("Código de rastreio não encontrado: " + trackingCode));

        List<TrackingEventResponse> events = trackingEventRepository
                .findByOrderIdOrderByEventTimeAsc(order.getId())
                .stream()
                .map(event -> new TrackingEventResponse(event.getStatus(), event.getLocation(), event.getEventTime()))
                .toList();

        return new TrackingResponse(
                order.getTrackingCode(),
                order.getOrderNumber(),
                order.getCustomer().getName(),
                order.getStatus(),
                events
        );
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        return new DashboardResponse(
                orderRepository.count(),
                orderRepository.countByStatus(DeliveryStatus.PICKING),
                orderRepository.countByStatus(DeliveryStatus.IN_TRANSIT),
                orderRepository.countByStatus(DeliveryStatus.OUT_FOR_DELIVERY),
                orderRepository.countByStatus(DeliveryStatus.DELIVERED),
                orderRepository.countByStatus(DeliveryStatus.DELIVERY_FAILED)
        );
    }

    @Transactional
    public OrderSummaryResponse updateStatus(Long id, StatusUpdateRequest request) {
        if (request == null || request.status() == null) {
            throw new IllegalArgumentException("O status da entrega é obrigatório.");
        }

        OrderEntity order = getOrder(id);
        List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(order.getId());

        if (request.status() == DeliveryStatus.PAYMENT_APPROVED) {
            inventoryService.reserveOrderItems(order, items);
        } else if (request.status() == DeliveryStatus.PICKING) {
            inventoryService.pickOrderItems(order, items);
        } else if (request.status() == DeliveryStatus.CANCELLED) {
            inventoryService.releaseOrderReservations(order);
        }

        order.setStatus(request.status());
        OrderEntity updatedOrder = orderRepository.save(order);

        if (request.status() == DeliveryStatus.READY_FOR_SHIPMENT
                || request.status() == DeliveryStatus.SHIPPED
                || request.status() == DeliveryStatus.IN_TRANSIT
                || request.status() == DeliveryStatus.OUT_FOR_DELIVERY) {
            orderDeliveryDetailsRepository.findByOrderId(updatedOrder.getId())
                    .ifPresent(delivery -> syncDispatchLocation(updatedOrder, delivery));
        }

        trackingEventRepository.save(new TrackingEvent(
                updatedOrder,
                request.status(),
                request.location(),
                OffsetDateTime.now()
        ));

        if (request.status() == DeliveryStatus.OUT_FOR_DELIVERY) {
            liveTrackingService.ensureSession(updatedOrder);
        }

        if (request.status() == DeliveryStatus.DELIVERED
                || request.status() == DeliveryStatus.DELIVERY_FAILED
                || request.status() == DeliveryStatus.RETURNED
                || request.status() == DeliveryStatus.CANCELLED) {
            liveTrackingService.deactivateForOrder(updatedOrder.getId());
        }

        return toSummary(updatedOrder);
    }

    @Transactional
    public OrderSummaryResponse confirmDelivery(Long id) {
        OrderEntity order = getOrder(id);
        order.setStatus(DeliveryStatus.DELIVERED);
        OrderEntity updatedOrder = orderRepository.save(order);

        trackingEventRepository.save(new TrackingEvent(
                updatedOrder,
                DeliveryStatus.DELIVERED,
                "Destino final",
                OffsetDateTime.now()
        ));

        liveTrackingService.deactivateForOrder(updatedOrder.getId());
        return toSummary(updatedOrder);
    }

    @Transactional
    public OrderSummaryResponse failDelivery(Long id) {
        OrderEntity order = getOrder(id);
        order.setStatus(DeliveryStatus.DELIVERY_FAILED);
        OrderEntity updatedOrder = orderRepository.save(order);

        trackingEventRepository.save(new TrackingEvent(
                updatedOrder,
                DeliveryStatus.DELIVERY_FAILED,
                "Tentativa de entrega sem sucesso",
                OffsetDateTime.now()
        ));

        liveTrackingService.deactivateForOrder(updatedOrder.getId());
        return toSummary(updatedOrder);
    }

    private void syncDispatchLocation(OrderEntity order, OrderDeliveryDetails delivery) {
        if (delivery.getLatitude() == null || delivery.getLongitude() == null) {
            return;
        }

        int priority = priorityWeight(order.getPriority());
        int slaMinutes = planningSlaMinutes(order.getDeliveryType(), delivery);
        String region = delivery.getCity() + "/" + delivery.getState();
        String destinationLabel = delivery.getAddressLabel() + " • " + region;

        DeliveryLocation location = deliveryLocationRepository.findByOrderId(order.getId())
                .orElseGet(() -> new DeliveryLocation(
                        order,
                        delivery.getLatitude(),
                        delivery.getLongitude(),
                        priority,
                        slaMinutes,
                        destinationLabel,
                        region
                ));

        location.updatePlanning(
                delivery.getLatitude(),
                delivery.getLongitude(),
                priority,
                slaMinutes,
                destinationLabel,
                region
        );
        deliveryLocationRepository.save(location);
    }

    private int priorityWeight(OrderPriority priority) {
        return switch (priority) {
            case URGENT -> 5;
            case HIGH -> 3;
            case NORMAL -> 1;
        };
    }

    private int planningSlaMinutes(DeliveryType deliveryType, OrderDeliveryDetails delivery) {
        if (delivery.getWindowEnd() != null) {
            LocalDateTime now = LocalDateTime.now(OPERATIONS_ZONE);
            LocalDateTime deadline = LocalDateTime.of(delivery.getDeliveryDate(), delivery.getWindowEnd());
            long minutes = Duration.between(now, deadline).toMinutes();
            if (minutes > 0) {
                return (int) Math.max(15, Math.min(1440, minutes));
            }
            return 15;
        }

        return switch (deliveryType) {
            case SAME_DAY -> 180;
            case EXPRESS -> 240;
            case SCHEDULED -> 360;
            case STANDARD -> 720;
        };
    }

    private void validateDeliveryDate(LocalDate deliveryDate, DeliveryType deliveryType) {
        LocalDate today = LocalDate.now(OPERATIONS_ZONE);
        if (deliveryDate == null) {
            throw new IllegalArgumentException("A data de entrega é obrigatória.");
        }
        if (deliveryDate.isBefore(today)) {
            throw new IllegalArgumentException("A data de entrega não pode estar no passado.");
        }
        if (deliveryType == DeliveryType.SAME_DAY && !deliveryDate.equals(today)) {
            throw new IllegalArgumentException("Pedidos SAME_DAY devem ter entrega na data atual.");
        }
    }

    private void validateTimeWindow(LocalTime start, LocalTime end, DeliveryType deliveryType) {
        if ((start == null) != (end == null)) {
            throw new IllegalArgumentException("Informe o início e o fim da janela de entrega.");
        }
        if (start != null && !end.isAfter(start)) {
            throw new IllegalArgumentException("O fim da janela de entrega deve ser posterior ao início.");
        }
        if (deliveryType == DeliveryType.SCHEDULED && start == null) {
            throw new IllegalArgumentException("Pedidos SCHEDULED exigem uma janela de entrega.");
        }
    }

    private OrderItem toEntity(OrderEntity order, OrderItemCreateRequest item, Product product) {
        return new OrderItem(
                order,
                product,
                product.getSku(),
                product.getName(),
                item.quantity(),
                product.getUnitPrice(),
                product.getWeightKg(),
                product.getVolumeM3()
        );
    }

    private Product getActiveProduct(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("O SKU é obrigatório para todos os itens do pedido.");
        }
        Product product = productRepository.findBySkuIgnoreCase(sku.trim())
                .orElseThrow(() -> new IllegalArgumentException("SKU não encontrado no catálogo: " + sku));
        if (!product.isActive()) {
            throw new IllegalArgumentException("O SKU está inativo e não pode ser incluído no pedido: " + product.getSku());
        }
        return product;
    }

    private String generateOrderNumber() {
        String value;
        do {
            value = "RC-" + Year.now(OPERATIONS_ZONE).getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (orderRepository.findByOrderNumber(value).isPresent());
        return value;
    }

    private String generateTrackingCode() {
        String value;
        do {
            value = "TRK-" + Year.now(OPERATIONS_ZONE).getValue() + "-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        } while (orderRepository.findByTrackingCode(value).isPresent());
        return value;
    }

    private OrderEntity getOrder(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O ID do pedido é obrigatório.");
        }

        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado. ID: " + id));
    }

    private OrderSummaryResponse toSummary(OrderEntity order) {
        if (order == null) {
            throw new IllegalArgumentException("Pedido não pode ser nulo.");
        }

        String customerName = order.getCustomer() != null
                ? order.getCustomer().getName()
                : "Cliente não informado";

        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                customerName,
                order.getTotal(),
                order.getStatus(),
                order.getPriority(),
                order.getDeliveryType(),
                order.getTrackingCode(),
                order.getCreatedAt()
        );
    }

    private OrderDetailResponse toDetail(
            OrderEntity order,
            List<OrderItem> items,
            OrderDeliveryDetails delivery
    ) {
        BigDecimal totalWeight = items.stream()
                .map(item -> item.getWeightKg().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVolume = items.stream()
                .map(item -> item.getVolumeM3().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalPackages = items.stream().mapToInt(OrderItem::getQuantity).sum();

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getTotal(),
                order.getStatus(),
                order.getPriority(),
                order.getDeliveryType(),
                order.getTrackingCode(),
                order.getCreatedAt(),
                totalWeight,
                totalVolume,
                totalPackages,
                delivery != null ? toDeliveryResponse(delivery) : null,
                items.stream().map(this::toItemResponse).toList()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getSku(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getWeightKg(),
                item.getVolumeM3(),
                item.getLineTotal()
        );
    }

    private OrderDeliveryDetailsResponse toDeliveryResponse(OrderDeliveryDetails delivery) {
        return new OrderDeliveryDetailsResponse(
                delivery.getCustomerAddressId(),
                delivery.getAddressLabel(),
                delivery.getStreet(),
                delivery.getNumber(),
                delivery.getComplement(),
                delivery.getDistrict(),
                delivery.getCity(),
                delivery.getState(),
                delivery.getZipCode(),
                delivery.getLatitude(),
                delivery.getLongitude(),
                delivery.getDeliveryDate(),
                delivery.getWindowStart(),
                delivery.getWindowEnd(),
                delivery.getInstructions()
        );
    }
}
