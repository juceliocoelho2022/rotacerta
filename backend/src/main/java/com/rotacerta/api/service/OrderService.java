package com.rotacerta.api.service;

import com.rotacerta.api.dto.DashboardResponse;
import com.rotacerta.api.dto.OrderSummaryResponse;
import com.rotacerta.api.dto.StatusUpdateRequest;
import com.rotacerta.api.dto.TrackingEventResponse;
import com.rotacerta.api.dto.TrackingResponse;
import com.rotacerta.api.model.DeliveryStatus;
import com.rotacerta.api.model.OrderEntity;
import com.rotacerta.api.model.TrackingEvent;
import com.rotacerta.api.repository.OrderRepository;
import com.rotacerta.api.repository.TrackingEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final TrackingEventRepository trackingEventRepository;

    public OrderService(OrderRepository orderRepository, TrackingEventRepository trackingEventRepository) {
        this.orderRepository = orderRepository;
        this.trackingEventRepository = trackingEventRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> findAll() {
        return orderRepository.findAll().stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public OrderSummaryResponse findById(Long id) {
        return toSummary(getOrder(id));
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
        order.setStatus(request.status());
        OrderEntity updatedOrder = orderRepository.save(order);

        trackingEventRepository.save(new TrackingEvent(
                updatedOrder,
                request.status(),
                request.location(),
                OffsetDateTime.now()
        ));

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

        return toSummary(updatedOrder);
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
                order.getTrackingCode(),
                order.getCreatedAt()
        );
    }
}
