package be.kdg.sa.backend.api.driver.dto;

import be.kdg.sa.backend.api.dto.AddressDto;
import be.kdg.sa.backend.domain.driver.Driver;

public record DriverDto(
        String id,
        String name,
        String email,
        String phoneNumber,
        String accountNumber,
        AddressDto address,
        String activeDeliveryId
) {
    public static DriverDto fromDomain(Driver driver) {
        return new DriverDto(
                driver.getId().id().toString(),
                driver.getName(),
                driver.getEmail(),
                driver.getPhoneNumber(),
                driver.getAccountNumber(),
                AddressDto.from(driver.getAddress()),
                driver.getActiveDeliveryId() != null ? driver.getActiveDeliveryId().toString() : null
        );
    }
}