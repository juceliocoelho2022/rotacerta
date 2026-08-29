package com.rotacerta.api.service;

import com.rotacerta.api.dto.AlternateRecipientRequest;
import com.rotacerta.api.dto.LiveLinkResponse;
import com.rotacerta.api.dto.LiveTrackingResponse;
import com.rotacerta.api.dto.TrackingEventResponse;
import com.rotacerta.api.model.DeliveryStatus;
import com.rotacerta.api.model.DeliveryTrackingSession;
import com.rotacerta.api.model.OrderEntity;
import com.rotacerta.api.repository.DeliveryTrackingSessionRepository;
import com.rotacerta.api.repository.OrderRepository;
import com.rotacerta.api.repository.TrackingEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LiveTrackingService {

    private static final int SESSION_HOURS = 48;

    private final DeliveryTrackingSessionRepository sessionRepository;
    private final OrderRepository orderRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final String publicBaseUrl;

    public LiveTrackingService(
            DeliveryTrackingSessionRepository sessionRepository,
            OrderRepository orderRepository,
            TrackingEventRepository trackingEventRepository,
            @Value("${ROTACERTA_PUBLIC_URL:http://localhost:5173}") String publicBaseUrl
    ) {
        this.sessionRepository = sessionRepository;
        this.orderRepository = orderRepository;
        this.trackingEventRepository = trackingEventRepository;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    @Transactional
    public DeliveryTrackingSession ensureSession(OrderEntity order) {
        DeliveryTrackingSession session = sessionRepository.findByOrderId(order.getId())
                .orElse(null);

        if (session == null) {
            session = new DeliveryTrackingSession(
                    order,
                    generateToken(),
                    OffsetDateTime.now().plusHours(SESSION_HOURS)
            );
        } else if (!session.isActive() || session.isExpired()) {
            session.renew(
                    generateToken(),
                    OffsetDateTime.now().plusHours(SESSION_HOURS)
            );
        }

        return sessionRepository.save(session);
    }

    @Transactional
    public LiveLinkResponse createOrGetLink(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado. ID: " + orderId));

        if (order.getStatus() != DeliveryStatus.OUT_FOR_DELIVERY) {
            throw new IllegalArgumentException("O RotaCerta Live só fica disponível quando o pedido sai para entrega.");
        }

        DeliveryTrackingSession session = ensureSession(order);
        return toLinkResponse(session);
    }

    @Transactional(readOnly = true)
    public LiveTrackingResponse getPublicTracking(String token) {
        DeliveryTrackingSession session = getValidSession(token);
        OrderEntity order = session.getOrder();

        List<TrackingEventResponse> events = trackingEventRepository
                .findByOrderIdOrderByEventTimeAsc(order.getId())
                .stream()
                .map(event -> new TrackingEventResponse(
                        event.getStatus(),
                        event.getLocation(),
                        event.getEventTime()
                ))
                .toList();

        return new LiveTrackingResponse(
                order.getTrackingCode(),
                order.getOrderNumber(),
                order.getCustomer().getName(),
                order.getStatus(),
                session.getExpiresAt(),
                session.getAlternateRecipientName(),
                session.getAlternateRecipientRelationship(),
                session.getDeliveryInstructions(),
                events
        );
    }

    @Transactional
    public LiveTrackingResponse authorizeAlternateRecipient(
            String token,
            AlternateRecipientRequest request
    ) {
        DeliveryTrackingSession session = getValidSession(token);
        session.updateRecipient(
                request.name().trim(),
                request.relationship().trim(),
                normalize(request.instructions())
        );
        sessionRepository.save(session);
        return getPublicTracking(token);
    }

    @Transactional
    public void deactivateForOrder(Long orderId) {
        sessionRepository.findByOrderId(orderId).ifPresent(session -> {
            session.deactivate();
            sessionRepository.save(session);
        });
    }

    private DeliveryTrackingSession getValidSession(String token) {
        DeliveryTrackingSession session = sessionRepository.findByPublicToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Link de acompanhamento inválido."));

        if (!session.isActive() || session.isExpired()) {
            throw new IllegalArgumentException("Este link de acompanhamento expirou ou foi encerrado.");
        }

        return session;
    }

    private LiveLinkResponse toLinkResponse(DeliveryTrackingSession session) {
        return new LiveLinkResponse(
                publicBaseUrl + "/live/" + session.getPublicToken(),
                session.getExpiresAt()
        );
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
