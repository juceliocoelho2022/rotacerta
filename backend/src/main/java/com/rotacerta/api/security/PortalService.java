package com.rotacerta.api.security;

import com.rotacerta.api.dto.OrderSummaryResponse;
import com.rotacerta.api.dto.PortalDtos.DriverPortalResponse;
import com.rotacerta.api.model.AppUser;
import com.rotacerta.api.model.Driver;
import com.rotacerta.api.model.OrderEntity;
import com.rotacerta.api.repository.AppUserRepository;
import com.rotacerta.api.repository.DriverRepository;
import com.rotacerta.api.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PortalService {

    private final AppUserRepository userRepository;
    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;

    public PortalService(
            AppUserRepository userRepository,
            OrderRepository orderRepository,
            DriverRepository driverRepository
    ) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> customerOrders(String email) {
        AppUser user = findUser(email);
        if (user.getCustomerId() == null) {
            return List.of();
        }
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(user.getCustomerId())
                .stream()
                .map(this::toOrderSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public DriverPortalResponse driverProfile(String email) {
        AppUser user = findUser(email);
        if (user.getDriverId() == null) {
            return new DriverPortalResponse(
                    null,
                    user.getDisplayName(),
                    user.getEmail(),
                    null,
                    false,
                    0,
                    0,
                    null,
                    null,
                    null
            );
        }

        Driver driver = driverRepository.findById(user.getDriverId())
                .orElseThrow(() -> new IllegalArgumentException("Motorista vinculado à conta não foi encontrado."));

        return new DriverPortalResponse(
                driver.getId(),
                user.getDisplayName(),
                user.getEmail(),
                driver.getName(),
                driver.isAvailable(),
                driver.getCurrentLoad(),
                driver.getMaxCapacity(),
                driver.getVehiclePlate(),
                driver.getVehicleModel(),
                driver.getPhotoUrl()
        );
    }

    private AppUser findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."));
    }

    private OrderSummaryResponse toOrderSummary(OrderEntity order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getName(),
                order.getTotal(),
                order.getStatus(),
                order.getPriority(),
                order.getDeliveryType(),
                order.getTrackingCode(),
                order.getCreatedAt()
        );
    }
}
