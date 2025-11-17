package be.kdg.sa.backend.domain.driver;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

import java.util.UUID;

@ValueObject
public record DriverId(UUID id) {
    public DriverId {
        Assert.notNull(id, "id cannot be null");
    }

}
