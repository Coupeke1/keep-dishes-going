package be.kdg.sa.backend.api.dto.restaurant;

import be.kdg.sa.backend.api.dto.AddressDto;

import java.time.Instant;
import java.util.UUID;

public record RestaurantOrderDto(
        UUID orderId,
        UUID restaurantId,
        String status,
        String decisionReason,
        Instant createdAt,
        String customerName,
        String customerEmail,
        AddressDto deliveryAddress
) {
}
