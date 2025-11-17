package be.kdg.sa.backend.domain.restaurant;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

import java.util.UUID;

@ValueObject
public record RestaurantId(UUID id) {
    public RestaurantId {
        Assert.notNull(id, "Id cannot be null");
    }

    public static RestaurantId create() {
        return new RestaurantId(UUID.randomUUID());
    }

}
