package be.kdg.sa.backend.domain.payment;

import be.kdg.sa.backend.domain.NotFoundException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.util.Assert;

@ValueObject
public record MolliePaymentId(String value) {
    public MolliePaymentId {
        Assert.notNull(value, "Id cannot be null");
    }

    public NotFoundException notFound() {
        return new NotFoundException("Order [" + value + "] not found");
    }
}
