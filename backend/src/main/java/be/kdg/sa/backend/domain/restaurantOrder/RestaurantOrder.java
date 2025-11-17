package be.kdg.sa.backend.domain.restaurantOrder;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import lombok.Getter;
import org.jmolecules.ddd.annotation.Identity;

import java.time.LocalDateTime;

@Getter
public class RestaurantOrder {

    @Identity
    private final RestaurantOrderId orderId;
    private final RestaurantId restaurantId;

    private final String customerName;
    private final String customerEmail;
    private final Address deliveryAddress;
    private final LocalDateTime createdAt;
    private OrderStatus status;
    private String decisionReason;

    private RestaurantOrder(RestaurantOrderId orderId,
                            RestaurantId restaurantId,
                            String customerName,
                            String customerEmail,
                            Address deliveryAddress,
                            OrderStatus status,
                            String decisionReason,
                            LocalDateTime createdAt) {
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.decisionReason = decisionReason;
        this.createdAt = createdAt;
    }

    public static RestaurantOrder reconstruct(
            RestaurantOrderId orderId,
            RestaurantId restaurantId,
            String customerName,
            String customerEmail,
            Address deliveryAddress,
            OrderStatus status,
            String decisionReason,
            LocalDateTime createdAt
    ) {
        return new RestaurantOrder(orderId, restaurantId, customerName, customerEmail, deliveryAddress, status, decisionReason, createdAt);
    }

    public static RestaurantOrder create(
            RestaurantId restaurantId,
            String customerName,
            String customerEmail,
            Address deliveryAddress
    ) {
        return new RestaurantOrder(RestaurantOrderId.create(), restaurantId, customerName, customerEmail, deliveryAddress, OrderStatus.PLACED, null, LocalDateTime.now());
    }

    public void accept(String reason) {
        ensureStatus(OrderStatus.PLACED);
        this.status = OrderStatus.ACCEPTED;
        this.decisionReason = reason;
    }

    public void reject(String reason) {
        ensureStatus(OrderStatus.PLACED);
        this.status = OrderStatus.CANCELLED;
        this.decisionReason = reason;
    }

    public void markReady() {
        ensureStatus(OrderStatus.ACCEPTED);
        this.status = OrderStatus.READY;
    }

    public void markPickedUp() {
        ensureStatus(OrderStatus.READY);
        this.status = OrderStatus.PICKED_UP;
    }

    public void markDelivered() {
        ensureStatus(OrderStatus.PICKED_UP);
        this.status = OrderStatus.DELIVERED;
    }

    private void ensureStatus(OrderStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException("Invalid status transition: expected " + expected + " but was " + this.status);
        }
    }
}
