package be.kdg.sa.backend.infrastructure.db.repositories.order.jpa;

import be.kdg.sa.backend.domain.payment.MolliePaymentId;
import be.kdg.sa.backend.domain.payment.Payment;
import be.kdg.sa.backend.domain.payment.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class JpaPaymentEntity {
    @Column(name = "payment_id")
    private String id;
    @Column(name = "payment_checkout_url")
    private String checkoutUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus status;

    protected JpaPaymentEntity() {}

    public JpaPaymentEntity(String id, String checkoutUrl, PaymentStatus status) {
        this.id = id;
        this.checkoutUrl = checkoutUrl;
        this.status = status;
    }

    public static JpaPaymentEntity fromDomain(Payment payment) {
        return new JpaPaymentEntity(
                payment.getId().value(),
                payment.getCheckoutUrl(),
                payment.getStatus()
        );
    }

    public Payment toDomain() {
        return new Payment(
                new MolliePaymentId(this.id),
                this.checkoutUrl,
                this.status
        );
    }
}
