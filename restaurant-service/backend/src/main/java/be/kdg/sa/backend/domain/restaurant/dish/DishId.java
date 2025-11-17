package be.kdg.sa.backend.domain.restaurant.dish;


import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

import java.util.UUID;

@ValueObject
public record DishId(UUID id) {
    public DishId {
        Assert.notNull(id, "Id cannot be null");
    }

    public static DishId create() {
        return new DishId(UUID.randomUUID());
    }

}
