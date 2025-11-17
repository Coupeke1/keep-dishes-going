package be.kdg.sa.backend.api.dto.restaurant;

import java.util.List;
import java.util.UUID;

public record RestaurantOverviewDto(
        UUID restaurantId,
        String name,
        boolean currentlyOpen,
        long pendingUpdatesCount,
        List<DishSummaryDto> dishes
) {
}