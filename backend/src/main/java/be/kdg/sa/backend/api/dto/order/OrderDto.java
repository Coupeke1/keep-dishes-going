package be.kdg.sa.backend.api.dto.order;


import be.kdg.sa.backend.domain.order.Order;
import be.kdg.sa.backend.domain.order.OrderLine;
import be.kdg.sa.backend.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderDto(UUID id, UUID restaurantId, OrderStatus status, List<OrderLineDto> lines, BigDecimal totalPrice, String paymentUrl) {
    public static OrderDto from(final Order order) {
        return new OrderDto(order.getId().value(), order.getRestaurantId() != null ? order.getRestaurantId().value() : null, order.getStatus(), order.getLines().stream().map(OrderLineDto::from).toList(), order.getTotalPrice(), order.getPayment() != null ? order.getPayment().getCheckoutUrl() : null);
    }

    public record OrderLineDto(UUID dishId, String dishName, BigDecimal unitPrice, BigDecimal totalPrice, int quantity, String notes) {
        public static OrderLineDto from(final OrderLine orderLine) {
            return new OrderLineDto(orderLine.getDishId().value(), orderLine.getDishName(), orderLine.getUnitPrice(), orderLine.getTotalPrice(), orderLine.getQuantity(), orderLine.getNotes());
        }
    }
}
