package be.kdg.sa.backend.infrastructure.db.repositories.order.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@Setter
public class JpaOrderLineId {
    @Column(name = "order_id")
    private UUID orderId;
    @Column(name = "dish_id")
    private UUID dishId;

    protected JpaOrderLineId() {}

    public JpaOrderLineId(UUID orderId, UUID dishId) {
        this.orderId = orderId;
        this.dishId = dishId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        JpaOrderLineId that = (JpaOrderLineId) o;
        return Objects.equals(orderId, that.orderId) && Objects.equals(dishId, that.dishId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, dishId);
    }
}
