package be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository.jpa;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import be.kdg.sa.backend.domain.restaurantOrder.OrderStatus;
import be.kdg.sa.backend.domain.restaurantOrder.RestaurantOrder;
import be.kdg.sa.backend.domain.restaurantOrder.RestaurantOrderId;
import be.kdg.sa.backend.events.OrderCreatedEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "restaurant_orders")
public class JpaRestaurantOrderEntity {
    @Id
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "delivery_street")
    private String deliveryStreet;

    @Column(name = "delivery_house_number")
    private String deliveryHouseNumber;

    @Column(name = "delivery_bus_number")
    private String deliveryBusNumber;

    @Column(name = "delivery_country")
    private String deliveryCountry;

    @Column(name = "delivery_city")
    private String deliveryCity;

    @Column(name = "delivery_postal_code")
    private String deliveryPostalCode;

    @Column(name = "status")
    private String status;

    @Column(name = "decision_reason")
    private String decisionReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static JpaRestaurantOrderEntity fromEvent(OrderCreatedEvent event) {
        JpaRestaurantOrderEntity entity = new JpaRestaurantOrderEntity();
        entity.orderId = event.orderId();
        entity.restaurantId = event.restaurantId();
        entity.status = OrderStatus.PLACED.name();

        if (event.timePlaced() != null) {
            try {
                entity.createdAt = LocalDateTime.parse(event.timePlaced());
            } catch (DateTimeParseException ex) {
                entity.createdAt = LocalDateTime.now();
            }
        } else {
            entity.createdAt = LocalDateTime.now();
        }

        if (event.customer() != null) {
            entity.customerName = event.customer().name();
            entity.customerEmail = event.customer().email();
            if (event.customer().address() != null) {
                entity.deliveryStreet = event.customer().address().street();
                entity.deliveryHouseNumber = event.customer().address().houseNumber();
                entity.deliveryBusNumber = event.customer().address().busNumber();
                entity.deliveryCountry = event.customer().address().country();
                entity.deliveryCity = event.customer().address().city();
                entity.deliveryPostalCode = event.customer().address().postalCode();
            }
        }

        return entity;
    }

    public static JpaRestaurantOrderEntity fromDomain(RestaurantOrder entity) {
        return new JpaRestaurantOrderEntity().toEntity(entity);
    }

    public void accept(String reason) {
        this.status = OrderStatus.ACCEPTED.name();
        this.decisionReason = reason;
    }

    public void reject(String reason) {
        this.status = OrderStatus.CANCELLED.name();
        this.decisionReason = reason;
    }

    public RestaurantOrder toDomain() {
        Address address = new Address(
                this.getDeliveryStreet(),
                this.getDeliveryHouseNumber(),
                this.getDeliveryBusNumber(),
                this.getDeliveryPostalCode(),
                this.getDeliveryCity(),
                this.getDeliveryCountry()
        );

        OrderStatus statusEnum;
        try {
            statusEnum = OrderStatus.valueOf(this.getStatus());
        } catch (Exception ex) {
            statusEnum = OrderStatus.PLACED;
        }

        return RestaurantOrder.reconstruct(
                new RestaurantOrderId(this.getOrderId()),
                new RestaurantId(this.restaurantId),
                this.getCustomerName(),
                this.getCustomerEmail(),
                address,
                statusEnum,
                this.getDecisionReason(),
                this.getCreatedAt()
        );
    }

    public JpaRestaurantOrderEntity toEntity(RestaurantOrder order) {
        JpaRestaurantOrderEntity entity = new JpaRestaurantOrderEntity();
        entity.setOrderId(order.getOrderId().id());
        entity.setRestaurantId(order.getRestaurantId().id());
        entity.setCustomerName(order.getCustomerName());
        entity.setCustomerEmail(order.getCustomerEmail());

        if (order.getDeliveryAddress() != null) {
            entity.setDeliveryStreet(order.getDeliveryAddress().street());
            entity.setDeliveryHouseNumber(order.getDeliveryAddress().houseNumber());
            entity.setDeliveryBusNumber(order.getDeliveryAddress().busNumber());
            entity.setDeliveryPostalCode(order.getDeliveryAddress().postalCode());
            entity.setDeliveryCity(order.getDeliveryAddress().city());
            entity.setDeliveryCountry(order.getDeliveryAddress().country());
        }

        entity.setStatus(order.getStatus() != null ? order.getStatus().name() : OrderStatus.PLACED.name());
        entity.setDecisionReason(order.getDecisionReason());
        entity.setCreatedAt(order.getCreatedAt());
        return entity;
    }

    public void updateFromDomain(RestaurantOrder entity) {
        this.setOrderId(entity.getOrderId().id());
        this.setRestaurantId(entity.getRestaurantId().id());
        this.setCustomerName(entity.getCustomerName());
        this.setCustomerEmail(entity.getCustomerEmail());

        if (entity.getDeliveryAddress() != null) {
            this.setDeliveryStreet(entity.getDeliveryAddress().street());
            this.setDeliveryHouseNumber(entity.getDeliveryAddress().houseNumber());
            this.setDeliveryBusNumber(entity.getDeliveryAddress().busNumber());
            this.setDeliveryPostalCode(entity.getDeliveryAddress().postalCode());
            this.setDeliveryCity(entity.getDeliveryAddress().city());
            this.setDeliveryCountry(entity.getDeliveryAddress().country());
        }

        this.setStatus(entity.getStatus().name());
        this.setDecisionReason(entity.getDecisionReason());
        this.setCreatedAt(entity.getCreatedAt());
    }
}
