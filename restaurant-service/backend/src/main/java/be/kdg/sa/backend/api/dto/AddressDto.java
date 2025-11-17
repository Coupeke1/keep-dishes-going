package be.kdg.sa.backend.api.dto;

import be.kdg.sa.backend.domain.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressDto(
        @NotBlank(message = "Street is required")
        String street,

        @NotBlank(message = "House number is required")
        String houseNumber,

        String busNumber,

        @NotBlank(message = "Postal code is required")
        @Pattern(regexp = "^[1-9][0-9]{3}", message = "Postal code must be a valid Belgian postal code")
        String postalCode,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Country is required")
        String country
) {
    public static AddressDto from(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressDto(
                address.street(),
                address.houseNumber(),
                address.busNumber(),
                address.postalCode(),
                address.city(),
                address.country()
        );
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
}
