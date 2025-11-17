package be.kdg.sa.backend.api.dto.restaurant;

import be.kdg.sa.backend.api.dto.AddressDto;
import be.kdg.sa.backend.api.dto.openingHours.CreateOpeningHoursDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRestaurantDto(
        @NotBlank(message = "A restaurant needs a name")
        String name,

        @NotNull(message = "Address is required")
        @Valid
        AddressDto address,

        @NotBlank(message = "Phone number is required")
        String phoneNumber,

        @NotBlank(message = "Email is required")
        String email,

        @Valid
        CreateOpeningHoursDto openingHours,

        @NotBlank(message = "Cuisine type is required")
        String cuisineType
) {
}