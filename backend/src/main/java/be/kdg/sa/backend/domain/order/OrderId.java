package be.kdg.sa.backend.domain.order;

import be.kdg.sa.backend.domain.NotFoundException;
import be.kdg.sa.backend.infrastructure.db.repositories.EntityNotFoundException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

import java.util.UUID;

@ValueObject
public record OrderId(UUID value) {
    public OrderId {
        Assert.notNull(value, "Id cannot be null");
    }

    public NotFoundException notFound() {
        return new NotFoundException("Order [" + value + "] not found");
    }

    public static OrderId create() {
        return new OrderId(UUID.randomUUID());
    }
}
