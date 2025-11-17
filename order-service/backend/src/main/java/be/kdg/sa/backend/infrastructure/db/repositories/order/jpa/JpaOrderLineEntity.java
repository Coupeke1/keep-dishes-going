package be.kdg.sa.backend.infrastructure.db.repositories.order.jpa;

import be.kdg.sa.backend.domain.restaurant.DishId;
import be.kdg.sa.backend.domain.order.OrderLine;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orderLines")
public class JpaOrderLineEntity {
    @EmbeddedId
    private JpaOrderLineId id;
    private String dishName;

    @Column(name = "unit_price",nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    private String notes;

    @MapsId("orderId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Setter
    private JpaOrderEntity order;

    protected JpaOrderLineEntity() {}

    public JpaOrderLineEntity(UUID orderId, UUID dishId, String dishName, BigDecimal unitPrice, int quantity, String notes) {
        this.id = new JpaOrderLineId(orderId, dishId);
        this.dishName = dishName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.notes = notes;
    }

    static JpaOrderLineEntity fromDomain(OrderLine orderLine, JpaOrderEntity jpaOrderEntity) {
        JpaOrderLineEntity jpaOrderLineEntity = new JpaOrderLineEntity(
                jpaOrderEntity.getId(),
                orderLine.getDishId().value(),
                orderLine.getDishName(),
                orderLine.getUnitPrice(),
                orderLine.getQuantity(),
                orderLine.getNotes()
        );
        jpaOrderLineEntity.order = jpaOrderEntity;
        return jpaOrderLineEntity;
    }

    public OrderLine toDomain() {
        return new OrderLine(
                new DishId(this.id.getDishId()),
                this.dishName,
                this.unitPrice,
                this.quantity,
                this.notes
        );
    }
}
