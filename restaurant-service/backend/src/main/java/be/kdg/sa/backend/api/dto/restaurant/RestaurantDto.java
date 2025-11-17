package be.kdg.sa.backend.api.dto.restaurant;

import be.kdg.sa.backend.api.dto.AddressDto;
import be.kdg.sa.backend.api.dto.openingHours.OpeningHoursDto;
import be.kdg.sa.backend.domain.restaurant.Restaurant;

import java.util.UUID;

public record RestaurantDto(
        UUID id,
        String name,
        AddressDto address,
        String cuisineType,
        String priceIndicator,
        OpeningHoursDto openingHours,
        boolean isOpen
) {
    public static RestaurantDto from(Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }
        return new RestaurantDto(
                restaurant.getId().id(),
                restaurant.getName(),
                AddressDto.from(restaurant.getAddress()),
                restaurant.getCuisineType(),
                restaurant.getPriceIndicator(),
                OpeningHoursDto.from(restaurant.getOpeningHours()),
                restaurant.isCurrentlyOpen()
        );
    }
}
