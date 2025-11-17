package be.kdg.sa.backend.domain.restaurant;

import be.kdg.sa.backend.domain.NotFoundException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

import java.util.UUID;

@ValueObject
public record RestaurantId(UUID value) {
    public RestaurantId {
        Assert.notNull(value, "Id cannot be null");
    }

    public NotFoundException notFound() {
        return new NotFoundException("Order [" + value + "] not found");
    }
    public static RestaurantId create() {
        return new RestaurantId(UUID.randomUUID());
    }
}
