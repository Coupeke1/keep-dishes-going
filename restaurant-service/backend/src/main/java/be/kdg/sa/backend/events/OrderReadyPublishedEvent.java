package be.kdg.sa.backend.events;

import java.util.UUID;

public record OrderReadyPublishedEvent(UUID orderId, UUID restaurantId) {

}
