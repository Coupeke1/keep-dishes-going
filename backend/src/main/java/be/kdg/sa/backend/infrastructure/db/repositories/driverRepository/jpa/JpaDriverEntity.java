package be.kdg.sa.backend.infrastructure.db.repositories.driverRepository.jpa;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.OrderId;
import be.kdg.sa.backend.domain.driver.Driver;
import be.kdg.sa.backend.domain.driver.DriverId;
import be.kdg.sa.backend.infrastructure.db.repositories.JpaAddressEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
@Table(name = "drivers")
public class JpaDriverEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;

    @Column(nullable = false)
    private String accountNumber;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false, foreignKey = @ForeignKey(name = "fk_driver_address"))
    private JpaAddressEntity address;

    @Column(name = "active_delivery_id")
    private UUID activeDeliveryId;

    protected JpaDriverEntity() {
    }

    public JpaDriverEntity(UUID id, String name, String email, String phoneNumber,
                           String accountNumber, JpaAddressEntity address,
                           UUID activeDeliveryId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.accountNumber = accountNumber;
        this.address = address;
        this.activeDeliveryId = activeDeliveryId;
    }

    public static JpaDriverEntity fromDomain(Driver driver) {
        if (driver == null) return null;

        return new JpaDriverEntity(
                driver.getId() != null ? driver.getId().id() : UUID.randomUUID(),
                driver.getName(),
                driver.getEmail(),
                driver.getPhoneNumber(),
                driver.getAccountNumber(),
                driver.getAddress() != null ? JpaAddressEntity.fromDomain(driver.getAddress()) : null,
                driver.getActiveDeliveryId() != null ? driver.getActiveDeliveryId().id() : null
        );
    }

    public void updateFromDomain(Driver driver) {
        if (driver == null) return;

        this.name = driver.getName();
        this.email = driver.getEmail();
        this.phoneNumber = driver.getPhoneNumber();
        this.accountNumber = driver.getAccountNumber();
        this.activeDeliveryId = driver.getActiveDeliveryId() != null ? driver.getActiveDeliveryId().id() : null;
        if (driver.getAddress() != null) {
            if (this.address == null) {
                this.address = JpaAddressEntity.fromDomain(driver.getAddress());
            } else {
                this.address.updateFromDomain(driver.getAddress());
            }
        } else {
            this.address = null;
        }
    }

    public Driver toDomain() {
        Address addressDomain = address != null ? address.toDomain() : null;
        OrderId orderId = activeDeliveryId != null ? new OrderId(activeDeliveryId) : null;

        return new Driver(
                new DriverId(id),
                name,
                email,
                phoneNumber,
                accountNumber,
                addressDomain,
                orderId

        );
    }
}
