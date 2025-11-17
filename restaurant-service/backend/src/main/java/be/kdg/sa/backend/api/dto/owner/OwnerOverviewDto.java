package be.kdg.sa.backend.api.dto.owner;

import be.kdg.sa.backend.api.dto.restaurant.RestaurantOverviewDto;

import java.util.UUID;

public record OwnerOverviewDto(UUID ownerId, RestaurantOverviewDto restaurant) {


}
