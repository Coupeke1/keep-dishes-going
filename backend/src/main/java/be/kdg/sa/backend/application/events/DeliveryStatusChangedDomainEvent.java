package be.kdg.sa.backend.application.events;

import be.kdg.sa.backend.domain.delivery.OrderStatus;

import java.util.UUID;

public record DeliveryStatusChangedDomainEvent(
        UUID orderId,
        OrderStatus status
) {
}
