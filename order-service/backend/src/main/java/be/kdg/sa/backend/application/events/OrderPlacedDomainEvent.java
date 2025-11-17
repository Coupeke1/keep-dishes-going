package be.kdg.sa.backend.application.events;

import java.util.UUID;

public record OrderPlacedDomainEvent(UUID orderId, UUID restaurantId) {

}
