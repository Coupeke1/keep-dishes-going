package be.kdg.sa.backend.domain.driver;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.OrderId;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

@AggregateRoot
@Getter
public class Driver {

    @Identity
    private final DriverId id;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final String accountNumber;
    private OrderId activeDeliveryId;
    @Setter
    private Address address;

    public Driver(DriverId id, String name, String email, String phoneNumber, String accountNumber, Address address, OrderId activeDeliveryId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.accountNumber = accountNumber;
        this.address = address;
        this.activeDeliveryId = activeDeliveryId;
    }

    public Driver(DriverId id, String name, String email, String phoneNumber, String accountNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.accountNumber = accountNumber;
    }

    public boolean hasActiveDelivery() {
        return activeDeliveryId != null;
    }

    public void markAsAssigned(OrderId deliveryId) {
        if (hasActiveDelivery())
            throw new IllegalStateException("Driver already has an active delivery.");
        this.activeDeliveryId = deliveryId;
    }

    public void completeDelivery() {
        if (activeDeliveryId == null)
            throw new IllegalStateException("No active delivery to complete.");
        this.activeDeliveryId = null;
    }

    public void markDeliveryCancelled() {
        if (activeDeliveryId == null)
            throw new IllegalStateException("No active delivery to cancel.");
        this.activeDeliveryId = null;
    }

    public void ensureCanCompleteDelivery() {
        if (!this.hasActiveDelivery()) {
            throw new IllegalStateException("Driver doesn't have an active delivery");
        }
    }

    public void ensureCanClaimDelivery() {
        if (this.hasActiveDelivery()) {
            throw new IllegalStateException("Driver already has an active delivery");
        }
    }
}
