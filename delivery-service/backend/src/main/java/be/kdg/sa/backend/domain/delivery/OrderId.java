package be.kdg.sa.backend.domain.delivery;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

import java.util.UUID;

@ValueObject
public record OrderId(UUID id) {
    public OrderId {
        Assert.notNull(id, "id cannot be null");
    }

}
