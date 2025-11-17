package be.kdg.sa.backend.domain.restaurantOrder;

import be.kdg.sa.backend.domain.NotFoundException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

import java.util.UUID;


@ValueObject
public record RestaurantOrderId(UUID id) {
    public RestaurantOrderId {
        Assert.notNull(id, "Id cannot be null");
    }

    public static RestaurantOrderId create() {
        return new RestaurantOrderId(UUID.randomUUID());
    }

    public NotFoundException notFound() {
        return new NotFoundException("Order [" + id + "] not found");
    }
}
