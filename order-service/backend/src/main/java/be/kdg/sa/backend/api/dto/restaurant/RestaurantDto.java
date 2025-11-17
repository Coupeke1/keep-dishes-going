package be.kdg.sa.backend.api.dto.restaurant;

import be.kdg.sa.backend.api.dto.AddressDto;
import be.kdg.sa.backend.domain.restaurant.Restaurant;

import java.util.UUID;

public record RestaurantDto(
        UUID id,
        String name,
        AddressDto address,
        String cuisineType,
        String priceIndicator,
        OpeningHoursDto openingHoursDto,
        boolean isOpen
) {
    public static RestaurantDto from(final Restaurant restaurant) {
        return new RestaurantDto(restaurant.id().value(), restaurant.name(), AddressDto.from(restaurant.address()), restaurant.cuisineType(), restaurant.priceIndicator(), OpeningHoursDto.from(restaurant.openingHours()), restaurant.isOpen());
    }
}
