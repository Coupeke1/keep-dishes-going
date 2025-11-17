package be.kdg.sa.backend.events;

import be.kdg.sa.backend.domain.delivery.OrderStatus;

import java.util.UUID;

public record DeliveryStatusChangedEvent(
        UUID orderId,
        OrderStatus status
) {
}
