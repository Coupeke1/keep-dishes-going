package be.kdg.sa.backend.domain.restaurant;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Restaurant(
        RestaurantId id,
        String name,
        Address address,
        String cuisineType,
        String priceIndicator,
        OpeningHours openingHours,
        boolean isOpen
) {
}
