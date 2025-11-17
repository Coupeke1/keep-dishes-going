package be.kdg.sa.backend.domain.owner;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

import java.util.UUID;


@ValueObject
public record OwnerId(UUID id) {
    public OwnerId {
        Assert.notNull(id, "OwnerId cannot be null");
    }

    public static OwnerId from(UUID id) {
        return new OwnerId(id);
    }
}
