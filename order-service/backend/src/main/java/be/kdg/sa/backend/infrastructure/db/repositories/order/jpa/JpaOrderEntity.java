package be.kdg.sa.backend.infrastructure.db.repositories.order.jpa;

import be.kdg.sa.backend.domain.payment.Payment;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import be.kdg.sa.backend.domain.order.Order;
import be.kdg.sa.backend.domain.order.OrderId;
import be.kdg.sa.backend.domain.order.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
public class JpaOrderEntity {
    @Id
    private UUID id;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    private LocalDateTime timePlaced;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Setter
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JpaOrderLineEntity> lines = new ArrayList<>();

    private BigDecimal totalPrice;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Embedded
    private JpaAddress deliveryAddress;
    @Embedded
    private JpaPaymentEntity payment;

    protected JpaOrderEntity() {}

    public JpaOrderEntity(UUID id, UUID restaurantId, LocalDateTime timePlaced, OrderStatus status, BigDecimal totalPrice, Payment payment) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.timePlaced = timePlaced;
        this.status = status;
        this.totalPrice = totalPrice;
        this.payment = payment != null ? JpaPaymentEntity.fromDomain(payment) : null;
    }

    public static JpaOrderEntity fromDomain(Order order) {
        JpaOrderEntity jpaOrderEntity = new JpaOrderEntity(
                order.getId().value(),
                order.getRestaurantId() != null ? order.getRestaurantId().value() : null,
                order.getTimePlaced(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getPayment()
        );

        List<JpaOrderLineEntity> JpaOrderLineEntities = order.getLines().stream()
                .map(line -> JpaOrderLineEntity.fromDomain(line, jpaOrderEntity))
                .toList();

        jpaOrderEntity.setLines(JpaOrderLineEntities);

        jpaOrderEntity.customerName = order.getCustomerName();
        jpaOrderEntity.customerEmail = order.getCustomerEmail();
        if (order.getDeliveryAddress() != null) {
            jpaOrderEntity.deliveryAddress = JpaAddress.fromDomain(order.getDeliveryAddress());
        }

        return jpaOrderEntity;
    }

    public Order toDomain() {
        RestaurantId domainRestaurantId = restaurantId != null ? new RestaurantId(restaurantId) : null;

        Order order = new Order(
                new OrderId(id),
                domainRestaurantId,
                timePlaced,
                status,
                totalPrice,
                this.customerName != null ? this.customerName : null,
                this.customerEmail != null ? this.customerEmail : null,
                this.deliveryAddress != null ? this.deliveryAddress.toDomain() : null,
                payment != null ? payment.toDomain() : null
        );
        order.setLines(new ArrayList<>(lines.stream()
                .map(JpaOrderLineEntity::toDomain)
                .toList()));
        return order;
    }
}
