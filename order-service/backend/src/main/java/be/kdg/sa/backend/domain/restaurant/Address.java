package be.kdg.sa.backend.domain.restaurant;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Address(
        String street, String houseNumber, String busNumber, String country, String city, String postalCode
) {
}