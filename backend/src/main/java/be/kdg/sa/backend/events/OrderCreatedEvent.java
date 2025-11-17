package be.kdg.sa.backend.events;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID restaurantId,
        Customer customer,
        List<OrderLine> lines,
        String timePlaced,
        BigDecimal totalPrice
) {
    public record Customer(String name, String email, Address address) {}
    public record Address(String street, String houseNumber, String country, String city, String postalCode) {}
    public record OrderLine(UUID dishId, String dishName, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
}
