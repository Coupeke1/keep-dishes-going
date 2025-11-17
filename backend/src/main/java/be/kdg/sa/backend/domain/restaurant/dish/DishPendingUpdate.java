package be.kdg.sa.backend.domain.restaurant.dish;

import org.jmolecules.ddd.annotation.ValueObject;

import java.time.LocalDateTime;

@ValueObject
public record DishPendingUpdate(
        String name,
        String description,
        Double price,
        Boolean vegetarian,
        Boolean vegan,
        Boolean glutenFree,
        DishCategory category,
        DishStatus status,
        LocalDateTime scheduledFor
) {
}
