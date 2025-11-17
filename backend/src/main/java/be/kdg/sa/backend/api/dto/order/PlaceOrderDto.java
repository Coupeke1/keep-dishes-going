package be.kdg.sa.backend.api.dto.order;

import be.kdg.sa.backend.api.dto.AddressDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderDto(
        @NotBlank(message = "Naam is verplicht") String name,
        @Email @NotBlank(message = "email is verplicht") String email,
        @NotNull AddressDto addressDto
) {

}
