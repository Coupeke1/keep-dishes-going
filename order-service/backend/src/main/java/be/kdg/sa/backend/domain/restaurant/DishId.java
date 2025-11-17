package be.kdg.sa.backend.domain.restaurant;

import be.kdg.sa.backend.domain.NotFoundException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

import java.util.UUID;

@ValueObject
public record DishId(UUID value) {
    public DishId {
        Assert.notNull(value, "Id cannot be null");
    }

    public NotFoundException notFound() {
        return new NotFoundException("Dish [" + value + "] not found");
    }
    public static DishId create() {
        return new DishId(UUID.randomUUID());
    }
}
