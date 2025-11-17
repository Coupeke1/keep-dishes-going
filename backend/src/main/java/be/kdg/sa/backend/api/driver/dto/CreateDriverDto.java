package be.kdg.sa.backend.api.driver.dto;

import be.kdg.sa.backend.api.dto.AddressDto;

public record CreateDriverDto(
        String name,
        String email,
        String phoneNumber,
        String accountNumber,
        AddressDto address
) {

}
