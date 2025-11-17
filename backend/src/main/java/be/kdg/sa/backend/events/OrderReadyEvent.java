package be.kdg.sa.backend.events;

import java.util.UUID;

public record OrderReadyEvent(UUID orderId, UUID restaurantId) {

}
