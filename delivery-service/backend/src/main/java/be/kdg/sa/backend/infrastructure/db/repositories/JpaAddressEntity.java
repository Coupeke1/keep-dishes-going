package be.kdg.sa.backend.infrastructure.db.repositories;

import be.kdg.sa.backend.domain.Address;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "addresses")
public class JpaAddressEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String street;

    @Column(name = "house_number", nullable = false)
    private String houseNumber;

    @Column(name = "bus_number")
    private String busNumber;

    @Column(name = "postal_code", nullable = false, length = 4)
    private String postalCode;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    protected JpaAddressEntity() {
    }

    public JpaAddressEntity(UUID id, String street, String houseNumber, String busNumber,
                            String postalCode, String city, String country) {
        this.id = id;
        this.street = street;
        this.houseNumber = houseNumber;
        this.busNumber = busNumber;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    public static JpaAddressEntity fromDomain(Address address) {
        if (address == null) {
            return null;
        }
        return new JpaAddressEntity(
                UUID.randomUUID(),
                address.street(),
                address.houseNumber(),
                address.busNumber(),
                address.postalCode(),
                address.city(),
                address.country()
        );
    }

    public void updateAddress(String street, String houseNumber, String busNumber,
                              String postalCode, String city, String country) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.busNumber = busNumber;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
    }

    public Address toDomain() {
        return new Address(
                street,
                houseNumber,
                busNumber,
                postalCode,
                city,
                country
        );
    }

    public void updateFromDomain(Address address) {
        Objects.requireNonNull(address, "Address is required");

        this.street = address.street();
        this.houseNumber = address.houseNumber();
        this.busNumber = address.busNumber();
        this.postalCode = address.postalCode();
        this.city = address.city();
        this.country = address.country();
    }
}