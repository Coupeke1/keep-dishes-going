package be.kdg.sa.backend.events;

import java.util.UUID;

public record OrderDecisionEvent(UUID orderId, String decision, String reason, Address restaurantAddress) {
    public record Address(
            String street,
            String houseNumber,
            String busNumber,
            String postalCode,
            String city,
            String country
    ) {
    }
}
