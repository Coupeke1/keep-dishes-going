package be.kdg.sa.backend.api.dto.dish;

import be.kdg.sa.backend.domain.restaurant.dish.Dish;

import java.util.UUID;

public record DishDto(
        UUID id,
        String name,
        String description,
        double price,
        boolean vegetarian,
        boolean vegan,
        boolean glutenFree,
        String category,
        String status
) {
    public static DishDto from(Dish dish) {
        return new DishDto(
                dish.getId().id(),
                dish.getName(),
                dish.getDescription(),
                dish.getPrice(),
                dish.isVegetarian(),
                dish.isVegan(),
                dish.isGlutenFree(),
                dish.getCategory().name(),
                dish.getStatus().name()
        );
    }
}
