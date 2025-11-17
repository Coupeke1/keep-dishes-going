package be.kdg.sa.backend.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Address(
        String street,
        String houseNumber,
        String busNumber,
        String postalCode,
        String city,
        String country
) {
    public Address {
        street = street != null ? street.trim() : null;
        houseNumber = houseNumber != null ? houseNumber.trim() : null;
        busNumber = busNumber != null ? busNumber.trim() : null;
        postalCode = postalCode != null ? postalCode.trim() : null;
        city = city != null ? city.trim() : null;
        country = country != null ? country.trim() : null;
    }
}