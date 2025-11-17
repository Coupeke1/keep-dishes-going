package be.kdg.sa.backend.infrastructure.db.repositories.order.jpa;

import be.kdg.sa.backend.domain.restaurant.Address;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class JpaAddress {
    private String street;
    private String houseNumber;
    private String busNumber;
    private String country;
    private String city;
    private String postalCode;

    protected JpaAddress() {}

    public JpaAddress(String street, String houseNumber, String busNumber, String country, String city, String postalCode) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.busNumber = busNumber;
        this.country = country;
        this.city = city;
        this.postalCode = postalCode;
    }

    public static JpaAddress fromDomain(Address address) {
        return new JpaAddress(
                address.street(),
                address.houseNumber(),
                address.busNumber(),
                address.country(),
                address.city(),
                address.postalCode()
        );
    }

    public Address toDomain() {
        return new Address(street, houseNumber, busNumber, country, city, postalCode);
    }
}
