package be.kdg.sa.backend.api.dto.restaurant;

import be.kdg.sa.backend.domain.restaurant.Dish;

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
        return  new DishDto(
                dish.dishId().value(),
                dish.name(),
                dish.description(),
                dish.price().doubleValue(),
                dish.vegetarian(),
                dish.vegan(),
                dish.glutenFree(),
                dish.category(),
                dish.status()
        );
    }
}
