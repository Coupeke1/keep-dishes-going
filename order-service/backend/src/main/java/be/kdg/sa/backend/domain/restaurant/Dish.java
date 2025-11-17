package be.kdg.sa.backend.domain.restaurant;

import org.jmolecules.ddd.annotation.ValueObject;

import java.math.BigDecimal;

@ValueObject
public record Dish(
        DishId dishId,
        String name,
        String description,
        BigDecimal price,
        boolean vegetarian,
        boolean vegan,
        boolean glutenFree,
        String category,
        String status
) {
}
