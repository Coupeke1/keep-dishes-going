package be.kdg.sa.backend.events;

import java.util.UUID;

public record OrderTimeoutEvent(UUID orderId) {}
