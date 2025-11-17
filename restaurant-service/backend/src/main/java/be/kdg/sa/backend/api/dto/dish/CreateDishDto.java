package be.kdg.sa.backend.api.dto.dish;

import be.kdg.sa.backend.domain.restaurant.dish.DishCategory;
import be.kdg.sa.backend.domain.restaurant.dish.DishStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateDishDto(
        @NotBlank(message = "Name is required") String name,
        String description,
        @NotNull
        @PositiveOrZero(message = "Price must not be negative")
        double price,
        boolean vegetarian,
        boolean vegan,
        boolean glutenFree,
        @NotNull(message = "Category is required") DishCategory category,
        @NotNull(message = "Status is required") DishStatus status

) {
}
