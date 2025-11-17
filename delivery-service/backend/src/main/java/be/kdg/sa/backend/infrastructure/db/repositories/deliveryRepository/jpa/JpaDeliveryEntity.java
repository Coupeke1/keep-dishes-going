package be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.jpa;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.domain.delivery.DeliveryStatus;
import be.kdg.sa.backend.domain.delivery.OrderId;
import be.kdg.sa.backend.domain.delivery.OrderStatus;
import be.kdg.sa.backend.domain.driver.DriverId;
import be.kdg.sa.backend.infrastructure.db.repositories.JpaAddressEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "deliveries")
public class JpaDeliveryEntity {

    @Id
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_address_id", nullable = false, foreignKey = @ForeignKey(name = "fk_delivery_pickup_address"))
    private JpaAddressEntity pickupAddress;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id", nullable = false, foreignKey = @ForeignKey(name = "fk_delivery_delivery_address"))
    private JpaAddressEntity deliveryAddress;

    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "assigned_driver_id")
    private UUID assignedDriverId;

    protected JpaDeliveryEntity() {
    }

    public JpaDeliveryEntity(UUID orderId,
                             DeliveryStatus status,
                             OrderStatus orderStatus,
                             JpaAddressEntity pickupAddress,
                             JpaAddressEntity deliveryAddress,
                             LocalDateTime pickupTime,
                             LocalDateTime deliveryTime,
                             BigDecimal price,
                             UUID assignedDriverId) {
        this.orderId = orderId;
        this.status = status;
        this.orderStatus = orderStatus;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.pickupTime = pickupTime;
        this.deliveryTime = deliveryTime;
        this.price = price;
        this.assignedDriverId = assignedDriverId;
    }

    public static JpaDeliveryEntity fromDomain(Delivery delivery) {
        if (delivery == null) return null;

        return new JpaDeliveryEntity(
                delivery.getOrderId() != null ? delivery.getOrderId().id() : UUID.randomUUID(),
                delivery.getStatus(),
                delivery.getOrderStatus(),
                JpaAddressEntity.fromDomain(delivery.getPickupAddress()),
                JpaAddressEntity.fromDomain(delivery.getDeliveryAddress()),
                delivery.getPickupTime(),
                delivery.getDeliveryTime(),
                delivery.getPrice(),
                delivery.getAssignedDriverId() != null ? delivery.getAssignedDriverId().id() : null
        );
    }

    public void updateFromDomain(Delivery delivery) {
        if (delivery == null) return;

        this.status = delivery.getStatus();
        this.orderStatus = delivery.getOrderStatus();
        this.pickupTime = delivery.getPickupTime();
        this.deliveryTime = delivery.getDeliveryTime();

        this.price = delivery.getPrice();

        this.assignedDriverId = delivery.getAssignedDriverId() != null ? delivery.getAssignedDriverId().id() : null;

        if (delivery.getPickupAddress() != null) {
            if (this.pickupAddress == null) {
                this.pickupAddress = JpaAddressEntity.fromDomain(delivery.getPickupAddress());
            } else {
                this.pickupAddress.updateFromDomain(delivery.getPickupAddress());
            }
        }

        if (delivery.getDeliveryAddress() != null) {
            if (this.deliveryAddress == null) {
                this.deliveryAddress = JpaAddressEntity.fromDomain(delivery.getDeliveryAddress());
            } else {
                this.deliveryAddress.updateFromDomain(delivery.getDeliveryAddress());
            }
        }
    }

    public Delivery toDomain() {
        Address pickup = pickupAddress != null ? pickupAddress.toDomain() : null;
        Address delivery = deliveryAddress != null ? deliveryAddress.toDomain() : null;
        DriverId driverId = assignedDriverId != null ? new DriverId(assignedDriverId) : null;

        return Delivery.reconstruct(
                new OrderId(orderId),
                status,
                orderStatus,
                pickup,
                delivery,
                pickupTime,
                deliveryTime,
                driverId,
                price
        );
    }
}
