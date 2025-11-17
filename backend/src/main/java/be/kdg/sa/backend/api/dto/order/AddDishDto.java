package be.kdg.sa.backend.api.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddDishDto(
        @NotNull UUID dishId,
        @NotNull UUID restaurantId,
        @Min(0) int quantity,
        String notes
) {
}
