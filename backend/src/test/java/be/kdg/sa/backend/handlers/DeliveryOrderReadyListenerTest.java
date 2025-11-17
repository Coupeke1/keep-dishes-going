package be.kdg.sa.backend.handlers;

import be.kdg.sa.backend.application.services.delivery.DeliveryService;
import be.kdg.sa.backend.events.OrderReadyForDeliveryEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryOrderReadyListenerTest {

    @Mock
    private DeliveryService deliveryService;

    private DeliveryOrderReadyListener listener;

    @BeforeEach
    void setUp() {
        listener = new DeliveryOrderReadyListener(deliveryService);
    }

    @Test
    @DisplayName("Handle order ready event should call delivery service with correct order ID")
    void handleOrderReady_ShouldCallDeliveryService() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(orderId, restaurantId);

        // Act
        listener.handleOrderReady(event);

        // Assert
        verify(deliveryService).DeliveryReady(orderId);
        verifyNoMoreInteractions(deliveryService);
    }

    @Test
    @DisplayName("Handle order ready event should work with same order and restaurant ID")
    void handleOrderReady_WithSameOrderAndRestaurantId_ShouldWork() {
        // Arrange
        UUID sameId = UUID.randomUUID();
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(sameId, sameId);

        // Act
        listener.handleOrderReady(event);

        // Assert
        verify(deliveryService).DeliveryReady(sameId);
    }

    @Test
    @DisplayName("Handle order ready event should work with minimum UUID values")
    void handleOrderReady_WithMinimumUUID_ShouldWork() {
        // Arrange
        UUID minUuid = new UUID(0L, 0L);
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(minUuid, minUuid);

        // Act
        listener.handleOrderReady(event);

        // Assert
        verify(deliveryService).DeliveryReady(minUuid);
    }

    @Test
    @DisplayName("Handle order ready event should work with maximum UUID values")
    void handleOrderReady_WithMaximumUUID_ShouldWork() {
        // Arrange
        UUID maxUuid = new UUID(-1L, -1L);
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(maxUuid, maxUuid);

        // Act
        listener.handleOrderReady(event);

        // Assert
        verify(deliveryService).DeliveryReady(maxUuid);
    }

    @Test
    @DisplayName("Handle multiple order ready events should call delivery service for each")
    void handleMultipleOrderReadyEvents_ShouldCallServiceForEach() {
        // Arrange
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();

        OrderReadyForDeliveryEvent event1 = new OrderReadyForDeliveryEvent(orderId1, restaurantId);
        OrderReadyForDeliveryEvent event2 = new OrderReadyForDeliveryEvent(orderId2, restaurantId);

        // Act
        listener.handleOrderReady(event1);
        listener.handleOrderReady(event2);

        // Assert
        verify(deliveryService).DeliveryReady(orderId1);
        verify(deliveryService).DeliveryReady(orderId2);
        verify(deliveryService, times(2)).DeliveryReady(any(UUID.class));
    }

    @Test
    @DisplayName("Handle order ready event should propagate delivery service exceptions")
    void handleOrderReady_WhenDeliveryServiceThrowsException_ShouldPropagate() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(orderId, restaurantId);

        doThrow(new RuntimeException("Delivery service error")).when(deliveryService).DeliveryReady(orderId);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> listener.handleOrderReady(event));
        verify(deliveryService).DeliveryReady(orderId);
    }

    @Test
    @DisplayName("Handle order ready event should propagate illegal state exceptions from delivery service")
    void handleOrderReady_WhenDeliveryServiceThrowsIllegalStateException_ShouldPropagate() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(orderId, restaurantId);

        doThrow(new IllegalStateException("Delivery already ready")).when(deliveryService).DeliveryReady(orderId);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> listener.handleOrderReady(event));
        verify(deliveryService).DeliveryReady(orderId);
    }

    @Test
    @DisplayName("Handle order ready event should work with null restaurant ID (if allowed by event)")
    void handleOrderReady_WithNullRestaurantId_ShouldCallService() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(orderId, null);

        // Act
        listener.handleOrderReady(event);

        // Assert
        verify(deliveryService).DeliveryReady(orderId);
    }

    @Test
    @DisplayName("Listener constructor with null delivery service should throw exception")
    void constructor_WithNullDeliveryService_ShouldThrowException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> new DeliveryOrderReadyListener(null));
    }

    @Test
    @DisplayName("Handle order ready event with null event should throw exception")
    void handleOrderReady_WithNullEvent_ShouldThrowException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> listener.handleOrderReady(null));
    }

    @Test
    @DisplayName("Handle order ready event should handle delivery service returning void normally")
    void handleOrderReady_WhenDeliveryServiceReturnsVoid_ShouldCompleteNormally() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        OrderReadyForDeliveryEvent event = new OrderReadyForDeliveryEvent(orderId, restaurantId);

        // This is the default behavior for a void method, but we're being explicit
        doNothing().when(deliveryService).DeliveryReady(orderId);

        // Act & Assert (should not throw)
        listener.handleOrderReady(event);

        // Assert
        verify(deliveryService).DeliveryReady(orderId);
    }

    @Test
    @DisplayName("Handle order ready event should work with different restaurant IDs for same order")
    void handleOrderReady_WithDifferentRestaurantIds_ShouldCallService() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID restaurantId1 = UUID.randomUUID();
        UUID restaurantId2 = UUID.randomUUID();

        OrderReadyForDeliveryEvent event1 = new OrderReadyForDeliveryEvent(orderId, restaurantId1);
        OrderReadyForDeliveryEvent event2 = new OrderReadyForDeliveryEvent(orderId, restaurantId2);

        // Act
        listener.handleOrderReady(event1);
        listener.handleOrderReady(event2);

        // Assert
        // Should call with the same order ID twice, even though restaurant IDs are different
        verify(deliveryService, times(2)).DeliveryReady(orderId);
    }

    @Test
    @DisplayName("Handle order ready event should work with sequential UUIDs")
    void handleOrderReady_WithSequentialUUIDs_ShouldWork() {
        // Arrange
        UUID orderId1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID orderId2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID restaurantId = UUID.randomUUID();

        OrderReadyForDeliveryEvent event1 = new OrderReadyForDeliveryEvent(orderId1, restaurantId);
        OrderReadyForDeliveryEvent event2 = new OrderReadyForDeliveryEvent(orderId2, restaurantId);

        // Act
        listener.handleOrderReady(event1);
        listener.handleOrderReady(event2);

        // Assert
        verify(deliveryService).DeliveryReady(orderId1);
        verify(deliveryService).DeliveryReady(orderId2);
    }
}