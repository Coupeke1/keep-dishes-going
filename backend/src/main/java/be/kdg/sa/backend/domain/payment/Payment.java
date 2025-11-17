package be.kdg.sa.backend.domain.payment;

import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
@Getter
public class Payment {
    private final MolliePaymentId id;
    private final String checkoutUrl;
    private PaymentStatus status;

    public Payment(MolliePaymentId id, String checkoutUrl, PaymentStatus status) {
        this.id = id;
        this.checkoutUrl = checkoutUrl;
        this.status = status;
    }

    public Payment(MolliePaymentId id, String checkoutUrl) {
        this.id = id;
        this.checkoutUrl = checkoutUrl;
        this.status = PaymentStatus.IN_PROGRESS;
    }

    public boolean isPaid() {
        return this.status == PaymentStatus.PAID;
    }

    public void markAsPaid() {
        if (status != PaymentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Payment not in progress");
        }
        this.status = PaymentStatus.PAID;
    }
}
