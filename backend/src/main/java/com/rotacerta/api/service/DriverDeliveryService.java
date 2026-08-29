package com.rotacerta.api.service;

import com.rotacerta.api.dto.DriverDeliveryResponse;
import com.rotacerta.api.model.DeliveryTrackingSession;
import com.rotacerta.api.model.OrderEntity;
import com.rotacerta.api.repository.DeliveryTrackingSessionRepository;
import com.rotacerta.api.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverDeliveryService {

    private final OrderRepository orderRepository;
    private final DeliveryTrackingSessionRepository sessionRepository;

    public DriverDeliveryService(
            OrderRepository orderRepository,
            DeliveryTrackingSessionRepository sessionRepository
    ) {
        this.orderRepository = orderRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public DriverDeliveryResponse findByOrderCode(String orderCode) {
        String normalizedCode = normalizeOrderCode(orderCode);

        OrderEntity order = orderRepository.findByOrderNumber(normalizedCode)
                .or(() -> orderRepository.findByTrackingCode(normalizedCode))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Entrega não encontrada para o código: " + normalizedCode
                ));

        DeliveryTrackingSession session = sessionRepository.findByOrderId(order.getId())
                .orElse(null);

        boolean recipientAuthorized = session != null
                && session.isActive()
                && !session.isExpired()
                && session.getAlternateRecipientName() != null
                && !session.getAlternateRecipientName().isBlank();

        return new DriverDeliveryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTrackingCode(),
                order.getCustomer().getName(),
                order.getStatus(),
                recipientAuthorized,
                recipientAuthorized ? session.getAlternateRecipientName() : null,
                recipientAuthorized ? session.getAlternateRecipientRelationship() : null,
                recipientAuthorized ? session.getDeliveryInstructions() : null,
                recipientAuthorized ? session.getRecipientUpdatedAt() : null
        );
    }

    private String normalizeOrderCode(String orderCode) {
        if (orderCode == null || orderCode.isBlank()) {
            throw new IllegalArgumentException("O código do pedido é obrigatório.");
        }

        String normalized = orderCode.trim();
        while (normalized.startsWith("#")) {
            normalized = normalized.substring(1).trim();
        }

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("O código do pedido é obrigatório.");
        }

        return normalized;
    }
}
