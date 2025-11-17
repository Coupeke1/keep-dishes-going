package be.kdg.sa.backend.api.delivery.dto;

import be.kdg.sa.backend.api.dto.AddressDto;

public record CreateDeliveryDto(
        AddressDto pickupAddress,
        AddressDto deliveryAddress
) {
}