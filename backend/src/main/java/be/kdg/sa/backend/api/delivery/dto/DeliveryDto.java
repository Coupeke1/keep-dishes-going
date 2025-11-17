package be.kdg.sa.backend.api.delivery.dto;

import be.kdg.sa.backend.api.dto.AddressDto;
import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.domain.delivery.DeliveryStatus;
import be.kdg.sa.backend.domain.delivery.OrderStatus;

import java.time.LocalDateTime;

public record DeliveryDto(
        String orderId,
        DeliveryStatus status,
        OrderStatus orderStatus,
        AddressDto pickupAddress,
        AddressDto deliveryAddress,
        LocalDateTime pickupTime,
        LocalDateTime deliveryTime,
        String assignedDriverId
) {
    public static DeliveryDto fromDomain(Delivery delivery) {
        return new DeliveryDto(
                delivery.getOrderId().id().toString(),
                delivery.getStatus(),
                delivery.getOrderStatus(),
                AddressDto.from(delivery.getPickupAddress()),
                AddressDto.from(delivery.getDeliveryAddress()),
                delivery.getPickupTime(),
                delivery.getDeliveryTime(),
                delivery.getAssignedDriverId() != null ? delivery.getAssignedDriverId().toString() : null
        );
    }
}