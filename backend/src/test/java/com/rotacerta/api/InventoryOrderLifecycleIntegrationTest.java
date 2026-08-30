package com.rotacerta.api;

import com.rotacerta.api.dto.OrderCreateRequest;
import com.rotacerta.api.dto.OrderDetailResponse;
import com.rotacerta.api.dto.OrderItemCreateRequest;
import com.rotacerta.api.dto.StatusUpdateRequest;
import com.rotacerta.api.model.Customer;
import com.rotacerta.api.model.CustomerAddress;
import com.rotacerta.api.model.DeliveryStatus;
import com.rotacerta.api.model.DeliveryType;
import com.rotacerta.api.model.Inventory;
import com.rotacerta.api.model.InventoryReservationStatus;
import com.rotacerta.api.model.OrderPriority;
import com.rotacerta.api.repository.CustomerAddressRepository;
import com.rotacerta.api.repository.CustomerRepository;
import com.rotacerta.api.repository.InventoryRepository;
import com.rotacerta.api.repository.InventoryReservationRepository;
import com.rotacerta.api.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class InventoryOrderLifecycleIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerAddressRepository customerAddressRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Test
    void paymentApprovalReservesSkuAndPickingConsumesPhysicalStock() {
        String sku = "MOUSE-LOGI-001";
        Inventory before = inventory(sku);
        int initialTotal = before.getTotalQuantity();
        int initialReserved = before.getReservedQuantity();

        OrderDetailResponse order = createOrder(sku, 2);

        assertThat(order.items()).hasSize(1);
        assertThat(order.items().getFirst().sku()).isEqualTo(sku);
        assertThat(order.items().getFirst().productName()).isEqualTo("Mouse Logitech");

        orderService.updateStatus(order.id(), new StatusUpdateRequest(DeliveryStatus.PAYMENT_APPROVED, "Teste CI"));

        Inventory reserved = inventory(sku);
        assertThat(reserved.getTotalQuantity()).isEqualTo(initialTotal);
        assertThat(reserved.getReservedQuantity()).isEqualTo(initialReserved + 2);
        assertThat(reserved.getAvailableQuantity()).isEqualTo(initialTotal - initialReserved - 2);

        var reservation = reservationRepository.findByOrderIdOrderByCreatedAtAsc(order.id()).getFirst();
        assertThat(reservation.getStatus()).isEqualTo(InventoryReservationStatus.RESERVED);
        assertThat(reservation.getQuantity()).isEqualTo(2);

        orderService.updateStatus(order.id(), new StatusUpdateRequest(DeliveryStatus.PICKING, "Separação CI"));

        Inventory picked = inventory(sku);
        assertThat(picked.getTotalQuantity()).isEqualTo(initialTotal - 2);
        assertThat(picked.getReservedQuantity()).isEqualTo(initialReserved);

        var confirmed = reservationRepository.findByOrderIdOrderByCreatedAtAsc(order.id()).getFirst();
        assertThat(confirmed.getStatus()).isEqualTo(InventoryReservationStatus.CONFIRMED);
    }

    @Test
    void cancellationReleasesActiveReservationWithoutChangingPhysicalStock() {
        String sku = "HEADSET-JBL-001";
        Inventory before = inventory(sku);
        int initialTotal = before.getTotalQuantity();
        int initialReserved = before.getReservedQuantity();

        OrderDetailResponse order = createOrder(sku, 1);
        orderService.updateStatus(order.id(), new StatusUpdateRequest(DeliveryStatus.PAYMENT_APPROVED, "Teste CI"));

        Inventory reserved = inventory(sku);
        assertThat(reserved.getReservedQuantity()).isEqualTo(initialReserved + 1);

        orderService.updateStatus(order.id(), new StatusUpdateRequest(DeliveryStatus.CANCELLED, "Cancelamento CI"));

        Inventory cancelled = inventory(sku);
        assertThat(cancelled.getTotalQuantity()).isEqualTo(initialTotal);
        assertThat(cancelled.getReservedQuantity()).isEqualTo(initialReserved);

        var released = reservationRepository.findByOrderIdOrderByCreatedAtAsc(order.id()).getFirst();
        assertThat(released.getStatus()).isEqualTo(InventoryReservationStatus.RELEASED);
    }

    private OrderDetailResponse createOrder(String sku, int quantity) {
        Customer customer = customerRepository.findAll().stream()
                .filter(candidate -> !customerAddressRepository
                        .findByCustomerIdOrderByPrimaryAddressDescCreatedAtAsc(candidate.getId())
                        .isEmpty())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("A base de testes precisa de um cliente com endereço."));

        CustomerAddress address = customerAddressRepository
                .findByCustomerIdOrderByPrimaryAddressDescCreatedAtAsc(customer.getId())
                .getFirst();

        return orderService.create(new OrderCreateRequest(
                customer.getId(),
                address.getId(),
                OrderPriority.NORMAL,
                DeliveryType.STANDARD,
                LocalDate.now().plusDays(1),
                null,
                null,
                List.of(new OrderItemCreateRequest(sku, quantity, null, null, null, null))
        ));
    }

    private Inventory inventory(String sku) {
        return inventoryRepository.findBySkuForUpdate(sku)
                .orElseThrow(() -> new IllegalStateException("SKU de teste não encontrado: " + sku));
    }
}
