package be.kdg.sa.backend.handlers;

import be.kdg.sa.backend.application.services.delivery.DeliveryService;
import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.domain.delivery.OrderId;
import be.kdg.sa.backend.events.DeliveryOrderEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryOrderListenerTest {

    @Mock
    private DeliveryService deliveryService;

    @InjectMocks
    private DeliveryOrderListener listener;

    @Test
    void handleNewDelivery_shouldProcessEventAndCreateDelivery() {
        // Given
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        DeliveryOrderEvent event = new DeliveryOrderEvent(
                orderId,
                restaurantId,
                new DeliveryOrderEvent.Customer(
                        "John Doe",
                        "john@example.com",
                        new DeliveryOrderEvent.Address(
                                "Customer Street", "123", "B", "Belgium", "Antwerp", "2000"
                        )
                ),
                new DeliveryOrderEvent.Address(
                        "Restaurant Street", "456", "A", "Belgium", "Brussels", "1000"
                ),
                List.of(
                        new DeliveryOrderEvent.OrderLine(
                                UUID.randomUUID(), "Pizza", 2, new BigDecimal("12.50"), new BigDecimal("25.00")
                        )
                ),
                "2024-01-01T12:00:00",
                new BigDecimal("25.00")
        );

        Delivery mockDelivery = new Delivery(
                new OrderId(orderId),
                null, null, null, null, null, null, null
        );

        when(deliveryService.createDelivery(any(UUID.class), any(Address.class), any(Address.class)))
                .thenReturn(mockDelivery);

        // When
        listener.handleNewDelivery(event);

        // Then
        ArgumentCaptor<UUID> orderIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Address> pickupAddressCaptor = ArgumentCaptor.forClass(Address.class);
        ArgumentCaptor<Address> deliveryAddressCaptor = ArgumentCaptor.forClass(Address.class);

        verify(deliveryService).createDelivery(
                orderIdCaptor.capture(),
                pickupAddressCaptor.capture(),
                deliveryAddressCaptor.capture()
        );

        assertThat(orderIdCaptor.getValue()).isEqualTo(orderId);

        // Verify pickup address (restaurant)
        Address capturedPickupAddress = pickupAddressCaptor.getValue();
        assertThat(capturedPickupAddress.street()).isEqualTo("Restaurant Street");
        assertThat(capturedPickupAddress.houseNumber()).isEqualTo("456");
        assertThat(capturedPickupAddress.busNumber()).isEqualTo("A");
        assertThat(capturedPickupAddress.postalCode()).isEqualTo("1000");
        assertThat(capturedPickupAddress.city()).isEqualTo("Brussels");
        assertThat(capturedPickupAddress.country()).isEqualTo("Belgium");

        // Verify delivery address (customer)
        Address capturedDeliveryAddress = deliveryAddressCaptor.getValue();
        assertThat(capturedDeliveryAddress.street()).isEqualTo("Customer Street");
        assertThat(capturedDeliveryAddress.houseNumber()).isEqualTo("123");
        assertThat(capturedDeliveryAddress.busNumber()).isEqualTo("B");
        assertThat(capturedDeliveryAddress.postalCode()).isEqualTo("2000");
        assertThat(capturedDeliveryAddress.city()).isEqualTo("Antwerp");
        assertThat(capturedDeliveryAddress.country()).isEqualTo("Belgium");
    }

    @Test
    void handleNewDelivery_shouldHandleNullBusNumbers() {
        // Given
        UUID orderId = UUID.randomUUID();

        DeliveryOrderEvent event = new DeliveryOrderEvent(
                orderId,
                UUID.randomUUID(),
                new DeliveryOrderEvent.Customer(
                        "Jane Doe",
                        "jane@example.com",
                        new DeliveryOrderEvent.Address(
                                "Customer Street", "123", null, "Belgium", "Antwerp", "2000"
                        )
                ),
                new DeliveryOrderEvent.Address(
                        "Restaurant Street", "456", null, "Belgium", "Brussels", "1000"
                ),
                List.of(),
                "2024-01-01T12:00:00",
                new BigDecimal("25.00")
        );

        Delivery mockDelivery = new Delivery(
                new OrderId(orderId),
                null, null, null, null, null, null, null
        );

        when(deliveryService.createDelivery(any(UUID.class), any(Address.class), any(Address.class)))
                .thenReturn(mockDelivery);

        // When
        listener.handleNewDelivery(event);

        // Then
        ArgumentCaptor<Address> pickupAddressCaptor = ArgumentCaptor.forClass(Address.class);
        ArgumentCaptor<Address> deliveryAddressCaptor = ArgumentCaptor.forClass(Address.class);

        verify(deliveryService).createDelivery(
                any(UUID.class),
                pickupAddressCaptor.capture(),
                deliveryAddressCaptor.capture()
        );

        // Verify null bus numbers are handled correctly
        Address capturedPickupAddress = pickupAddressCaptor.getValue();
        assertThat(capturedPickupAddress.busNumber()).isNull();

        Address capturedDeliveryAddress = deliveryAddressCaptor.getValue();
        assertThat(capturedDeliveryAddress.busNumber()).isNull();
    }

    @Test
    void handleNewDelivery_shouldHandleEmptyBusNumberStrings() {
        // Given
        UUID orderId = UUID.randomUUID();

        DeliveryOrderEvent event = new DeliveryOrderEvent(
                orderId,
                UUID.randomUUID(),
                new DeliveryOrderEvent.Customer(
                        "Bob Smith",
                        "bob@example.com",
                        new DeliveryOrderEvent.Address(
                                "Customer Street", "123", "", "Belgium", "Antwerp", "2000"
                        )
                ),
                new DeliveryOrderEvent.Address(
                        "Restaurant Street", "456", "   ", "Belgium", "Brussels", "1000"
                ),
                List.of(),
                "2024-01-01T12:00:00",
                new BigDecimal("25.00")
        );

        Delivery mockDelivery = new Delivery(
                new OrderId(orderId),
                null, null, null, null, null, null, null
        );

        when(deliveryService.createDelivery(any(UUID.class), any(Address.class), any(Address.class)))
                .thenReturn(mockDelivery);

        // When
        listener.handleNewDelivery(event);

        // Then
        ArgumentCaptor<Address> pickupAddressCaptor = ArgumentCaptor.forClass(Address.class);
        ArgumentCaptor<Address> deliveryAddressCaptor = ArgumentCaptor.forClass(Address.class);

        verify(deliveryService).createDelivery(
                any(UUID.class),
                pickupAddressCaptor.capture(),
                deliveryAddressCaptor.capture()
        );

        // Verify empty bus number strings are preserved (Address constructor will trim them)
        Address capturedPickupAddress = pickupAddressCaptor.getValue();
        assertThat(capturedPickupAddress.busNumber()).isEqualTo(""); // Empty string becomes empty after trim

        Address capturedDeliveryAddress = deliveryAddressCaptor.getValue();
        assertThat(capturedDeliveryAddress.busNumber()).isEqualTo(""); // Whitespace becomes empty after trim
    }

    @Test
    void handleNewDelivery_shouldHandleMinimalAddressData() {
        // Given
        UUID orderId = UUID.randomUUID();

        DeliveryOrderEvent event = new DeliveryOrderEvent(
                orderId,
                UUID.randomUUID(),
                new DeliveryOrderEvent.Customer(
                        "Minimal Customer",
                        "minimal@example.com",
                        new DeliveryOrderEvent.Address(
                                "Street", "1", null, "Country", "City", "1000"
                        )
                ),
                new DeliveryOrderEvent.Address(
                        "Restaurant St", "2", null, "Country", "City", "2000"
                ),
                List.of(),
                "2024-01-01T12:00:00",
                new BigDecimal("15.00")
        );

        Delivery mockDelivery = new Delivery(
                new OrderId(orderId),
                null, null, null, null, null, null, null
        );

        when(deliveryService.createDelivery(any(UUID.class), any(Address.class), any(Address.class)))
                .thenReturn(mockDelivery);

        // When
        listener.handleNewDelivery(event);

        // Then
        ArgumentCaptor<Address> pickupAddressCaptor = ArgumentCaptor.forClass(Address.class);
        ArgumentCaptor<Address> deliveryAddressCaptor = ArgumentCaptor.forClass(Address.class);

        verify(deliveryService).createDelivery(
                any(UUID.class),
                pickupAddressCaptor.capture(),
                deliveryAddressCaptor.capture()
        );

        // Verify minimal address data is handled correctly
        Address capturedPickupAddress = pickupAddressCaptor.getValue();
        assertThat(capturedPickupAddress.street()).isEqualTo("Restaurant St");
        assertThat(capturedPickupAddress.houseNumber()).isEqualTo("2");
        assertThat(capturedPickupAddress.busNumber()).isNull();
        assertThat(capturedPickupAddress.postalCode()).isEqualTo("2000");

        Address capturedDeliveryAddress = deliveryAddressCaptor.getValue();
        assertThat(capturedDeliveryAddress.street()).isEqualTo("Street");
        assertThat(capturedDeliveryAddress.houseNumber()).isEqualTo("1");
        assertThat(capturedDeliveryAddress.busNumber()).isNull();
        assertThat(capturedDeliveryAddress.postalCode()).isEqualTo("1000");
    }

    @Test
    void handleNewDelivery_shouldLogAppropriateInformation() {
        // Given
        UUID orderId = UUID.randomUUID();

        DeliveryOrderEvent event = new DeliveryOrderEvent(
                orderId,
                UUID.randomUUID(),
                new DeliveryOrderEvent.Customer(
                        "Test Customer",
                        "test@example.com",
                        new DeliveryOrderEvent.Address(
                                "Street", "123", "BusA", "Belgium", "City", "1000"
                        )
                ),
                new DeliveryOrderEvent.Address(
                        "Restaurant St", "456", "BusB", "Belgium", "City", "2000"
                ),
                List.of(),
                "2024-01-01T12:00:00",
                new BigDecimal("20.00")
        );

        Delivery mockDelivery = new Delivery(
                new OrderId(orderId),
                null, null, null, null, null, null, null
        );

        when(deliveryService.createDelivery(any(UUID.class), any(Address.class), any(Address.class)))
                .thenReturn(mockDelivery);

        // When
        listener.handleNewDelivery(event);

        // Then - verification is done through the service call
        // The logging is side effect that we can't easily test in unit tests without
        // using a logging framework specific test tool
        verify(deliveryService).createDelivery(any(UUID.class), any(Address.class), any(Address.class));
    }

    @Test
    void handleNewDelivery_shouldHandleEventWithMultipleOrderLines() {
        // Given
        UUID orderId = UUID.randomUUID();

        DeliveryOrderEvent event = new DeliveryOrderEvent(
                orderId,
                UUID.randomUUID(),
                new DeliveryOrderEvent.Customer(
                        "Multi Order Customer",
                        "multi@example.com",
                        new DeliveryOrderEvent.Address(
                                "Multi St", "999", null, "Belgium", "City", "3000"
                        )
                ),
                new DeliveryOrderEvent.Address(
                        "Multi Restaurant", "888", null, "Belgium", "City", "4000"
                ),
                List.of(
                        new DeliveryOrderEvent.OrderLine(
                                UUID.randomUUID(), "Pizza", 1, new BigDecimal("12.00"), new BigDecimal("12.00")
                        ),
                        new DeliveryOrderEvent.OrderLine(
                                UUID.randomUUID(), "Pasta", 2, new BigDecimal("8.50"), new BigDecimal("17.00")
                        ),
                        new DeliveryOrderEvent.OrderLine(
                                UUID.randomUUID(), "Salad", 1, new BigDecimal("6.00"), new BigDecimal("6.00")
                        )
                ),
                "2024-01-01T12:00:00",
                new BigDecimal("35.00")
        );

        Delivery mockDelivery = new Delivery(
                new OrderId(orderId),
                null, null, null, null, null, null, null
        );

        when(deliveryService.createDelivery(any(UUID.class), any(Address.class), any(Address.class)))
                .thenReturn(mockDelivery);

        // When
        listener.handleNewDelivery(event);

        // Then
        verify(deliveryService).createDelivery(any(UUID.class), any(Address.class), any(Address.class));
        // The order lines and total price don't affect delivery creation, so we just verify the service was called
    }
}