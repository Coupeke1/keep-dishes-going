package be.kdg.sa.backend.api.dto.dish;

import be.kdg.sa.backend.domain.restaurant.dish.DishCategory;
import be.kdg.sa.backend.domain.restaurant.dish.DishStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record UpdateDishDto(
        @NotBlank(message = "Name is required") String name,
        String description,
        @NotNull(message = "Price can't be null")
        @PositiveOrZero(message = "Price can't be negative")
        Double price,
        Boolean vegetarian,
        Boolean vegan,
        Boolean glutenFree,
        @NotNull(message = "Category is required") DishCategory category,
        @NotNull(message = "Status is required") DishStatus status,
        LocalDateTime scheduledFor
) {
}
