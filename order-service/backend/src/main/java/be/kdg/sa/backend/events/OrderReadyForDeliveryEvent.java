package be.kdg.sa.backend.events;

import java.util.UUID;

public record OrderReadyForDeliveryEvent(UUID orderId, UUID restaurantId) {}
