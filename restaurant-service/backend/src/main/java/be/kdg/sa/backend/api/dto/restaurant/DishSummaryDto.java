package be.kdg.sa.backend.api.dto.restaurant;

import be.kdg.sa.backend.domain.restaurant.dish.DishStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DishSummaryDto(
        UUID dishId,
        String name,
        String description,
        double price,
        DishStatus status,
        boolean hasPendingUpdate,
        LocalDateTime scheduledFor
) {
}