package be.kdg.sa.backend.domain.delivery;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.driver.DriverId;
import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
public class Delivery {
    @Identity
    private final OrderId orderId;
    private DeliveryStatus status;
    private OrderStatus orderStatus;
    private final Address pickupAddress;
    private final Address deliveryAddress;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private DriverId assignedDriverId;
    private BigDecimal price;


    public Delivery(OrderId orderId, DeliveryStatus status, OrderStatus orderStatus, Address pickupAddress, Address deliveryAddress, LocalDateTime pickupTime, LocalDateTime deliveryTime, DriverId driverId) {
        this.orderId = orderId;
        this.status = status;
        this.orderStatus = orderStatus;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.pickupTime = pickupTime;
        this.deliveryTime = deliveryTime;
        this.assignedDriverId = driverId;
    }

    public Delivery(OrderId orderId, DeliveryStatus status, OrderStatus orderStatus, Address pickupAddress, Address deliveryAddress) {
        this.orderId = orderId;
        this.status = status;
        this.orderStatus = orderStatus;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
    }

    public static Delivery createNew(OrderId orderId, Address pickup, Address delivery) {
        return new Delivery(orderId, DeliveryStatus.OPEN, OrderStatus.ACCEPTED, pickup, delivery);
    }

    public static Delivery reconstruct(OrderId orderId, DeliveryStatus status, OrderStatus orderStatus, Address pickup, Address delivery, LocalDateTime pickupTime, LocalDateTime deliveryTime, DriverId driverId, BigDecimal price) {
        Delivery d = new Delivery(orderId, status, orderStatus, pickup, delivery, pickupTime, deliveryTime, driverId);
        d.price = price;
        return d;
    }

    public void claim(DriverId driverId) {
        if (status != DeliveryStatus.OPEN || orderStatus != OrderStatus.READY && orderStatus != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Only open and accepted deliveries can be claimed.");
        }
        this.assignedDriverId = driverId;
        this.status = DeliveryStatus.CLAIMED;
    }

    public void start() {
        if (status != DeliveryStatus.CLAIMED || orderStatus != OrderStatus.READY)
            throw new IllegalStateException("Cannot start unless in claimed and ready");
        this.status = DeliveryStatus.IN_PROGRESS;
        this.orderStatus = OrderStatus.PICKED_UP;
        this.pickupTime = LocalDateTime.now();
    }

    public void complete(BigDecimal payout) {
        if (status != DeliveryStatus.IN_PROGRESS || orderStatus != OrderStatus.PICKED_UP)
            throw new IllegalStateException("Cannot complete unless in progress.");
        if (payout == null) throw new IllegalArgumentException("betaling mag niet null zijn");
        this.status = DeliveryStatus.DELIVERED;
        this.orderStatus = OrderStatus.DELIVERED;
        this.deliveryTime = LocalDateTime.now();
        this.price = payout;
    }

    public void cancel() {
        if (orderStatus == OrderStatus.READY) {
            throw new IllegalStateException("Cannot cancel a delivery that is already made");
        }
        this.status = DeliveryStatus.OPEN;
    }

    public void markAsReady() {
        if (this.orderStatus != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("A delivery can't be ready unless it's accepted");
        }
        this.orderStatus = OrderStatus.READY;
    }
}