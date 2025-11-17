package be.kdg.sa.backend.domain.delivery;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.driver.DriverId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DeliveryTest {

    @Test
    void createNew_shouldCreateDeliveryWithCorrectInitialState() {
        // Given
        OrderId orderId = new OrderId(UUID.randomUUID());
        Address pickupAddress = new Address("Pickup St", "1", null, "1000", "Brussels", "Belgium");
        Address deliveryAddress = new Address("Delivery St", "2", "A", "2000", "Antwerp", "Belgium");

        // When
        Delivery delivery = Delivery.createNew(orderId, pickupAddress, deliveryAddress);

        // Then
        assertThat(delivery.getOrderId()).isEqualTo(orderId);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.OPEN);
        assertThat(delivery.getOrderStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(delivery.getPickupAddress()).isEqualTo(pickupAddress);
        assertThat(delivery.getDeliveryAddress()).isEqualTo(deliveryAddress);
        assertThat(delivery.getPickupTime()).isNull();
        assertThat(delivery.getDeliveryTime()).isNull();
        assertThat(delivery.getAssignedDriverId()).isNull();
        assertThat(delivery.getPrice()).isNull();
    }

    @Test
    void reconstruct_shouldCreateDeliveryWithAllFields() {
        // Given
        OrderId orderId = new OrderId(UUID.randomUUID());
        Address pickupAddress = new Address("Pickup St", "1", null, "1000", "Brussels", "Belgium");
        Address deliveryAddress = new Address("Delivery St", "2", "A", "2000", "Antwerp", "Belgium");
        LocalDateTime pickupTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime deliveryTime = LocalDateTime.of(2024, 1, 1, 10, 30);
        DriverId driverId = new DriverId(UUID.randomUUID());
        BigDecimal price = new BigDecimal("15.50");

        // When
        Delivery delivery = Delivery.reconstruct(orderId, DeliveryStatus.DELIVERED, OrderStatus.DELIVERED,
                pickupAddress, deliveryAddress, pickupTime, deliveryTime, driverId, price);

        // Then
        assertThat(delivery.getOrderId()).isEqualTo(orderId);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(delivery.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(delivery.getPickupAddress()).isEqualTo(pickupAddress);
        assertThat(delivery.getDeliveryAddress()).isEqualTo(deliveryAddress);
        assertThat(delivery.getPickupTime()).isEqualTo(pickupTime);
        assertThat(delivery.getDeliveryTime()).isEqualTo(deliveryTime);
        assertThat(delivery.getAssignedDriverId()).isEqualTo(driverId);
        assertThat(delivery.getPrice()).isEqualTo(price);
    }

    @Test
    void claim_shouldAssignDriverAndUpdateStatusWhenOpenAndAccepted() {
        // Given
        Delivery delivery = createBasicDelivery();
        DriverId driverId = new DriverId(UUID.randomUUID());

        // When
        delivery.claim(driverId);

        // Then
        assertThat(delivery.getAssignedDriverId()).isEqualTo(driverId);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CLAIMED);
        assertThat(delivery.getOrderStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    void claim_shouldAssignDriverAndUpdateStatusWhenOpenAndReady() {
        // Given
        Delivery delivery = createBasicDelivery();
        delivery.markAsReady();
        DriverId driverId = new DriverId(UUID.randomUUID());

        // When
        delivery.claim(driverId);

        // Then
        assertThat(delivery.getAssignedDriverId()).isEqualTo(driverId);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CLAIMED);
        assertThat(delivery.getOrderStatus()).isEqualTo(OrderStatus.READY);
    }

    @Test
    void claim_shouldThrowExceptionWhenNotOpen() {
        // Given
        Delivery delivery = createBasicDelivery();
        delivery.claim(new DriverId(UUID.randomUUID()));
        DriverId anotherDriverId = new DriverId(UUID.randomUUID());

        // When & Then
        assertThatThrownBy(() -> delivery.claim(anotherDriverId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only open and accepted deliveries can be claimed.");
    }

    @Test
    void claim_shouldThrowExceptionWhenOrderStatusNotAcceptedOrReady() {
        // Given
        OrderId orderId = new OrderId(UUID.randomUUID());
        Address pickupAddress = new Address("Pickup St", "1", null, "1000", "Brussels", "Belgium");
        Address deliveryAddress = new Address("Delivery St", "2", "A", "2000", "Antwerp", "Belgium");

        Delivery delivery = new Delivery(orderId, DeliveryStatus.OPEN, OrderStatus.PLACED,
                pickupAddress, deliveryAddress);
        DriverId driverId = new DriverId(UUID.randomUUID());

        // When & Then
        assertThatThrownBy(() -> delivery.claim(driverId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only open and accepted deliveries can be claimed.");
    }

    @Test
    void markAsReady_shouldUpdateOrderStatusToReady() {
        // Given
        Delivery delivery = createBasicDelivery();

        // When
        delivery.markAsReady();

        // Then
        assertThat(delivery.getOrderStatus()).isEqualTo(OrderStatus.READY);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.OPEN);
    }

    @Test
    void markAsReady_shouldThrowExceptionWhenNotAccepted() {
        // Given
        OrderId orderId = new OrderId(UUID.randomUUID());
        Address pickupAddress = new Address("Pickup St", "1", null, "1000", "Brussels", "Belgium");
        Address deliveryAddress = new Address("Delivery St", "2", "A", "2000", "Antwerp", "Belgium");

        Delivery delivery = new Delivery(orderId, DeliveryStatus.OPEN, OrderStatus.PLACED,
                pickupAddress, deliveryAddress);

        // When & Then
        assertThatThrownBy(delivery::markAsReady)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A delivery can't be ready unless it's accepted");
    }

    @Test
    void start_shouldUpdateStatusAndSetPickupTime() {
        // Given
        Delivery delivery = createBasicDelivery();
        DriverId driverId = new DriverId(UUID.randomUUID());
        delivery.claim(driverId);
        delivery.markAsReady();

        LocalDateTime beforeStart = LocalDateTime.now();

        // When
        delivery.start();

        // Then
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.IN_PROGRESS);
        assertThat(delivery.getPickupTime()).isNotNull();
        assertThat(delivery.getPickupTime()).isAfterOrEqualTo(beforeStart);
        assertThat(delivery.getOrderStatus()).isEqualTo(OrderStatus.PICKED_UP);
    }

    @Test
    void start_shouldThrowExceptionWhenNotClaimed() {
        // Given
        Delivery delivery = createBasicDelivery();
        delivery.markAsReady();

        // When & Then
        assertThatThrownBy(delivery::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot start unless in claimed and ready");
    }

    @Test
    void start_shouldThrowExceptionWhenNotReady() {
        // Given
        Delivery delivery = createBasicDelivery();
        DriverId driverId = new DriverId(UUID.randomUUID());
        delivery.claim(driverId);

        // When & Then
        assertThatThrownBy(delivery::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot start unless in claimed and ready");
    }

    @Test
    void complete_shouldUpdateStatusAndSetDeliveryTimeAndPrice() {
        // Given
        Delivery delivery = createDeliveryInProgress();
        BigDecimal payout = new BigDecimal("12.50");
        LocalDateTime beforeComplete = LocalDateTime.now();

        // When
        delivery.complete(payout);

        // Then
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(delivery.getDeliveryTime()).isNotNull();
        assertThat(delivery.getDeliveryTime()).isAfterOrEqualTo(beforeComplete);
        assertThat(delivery.getPrice()).isEqualTo(payout);
        assertThat(delivery.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void complete_shouldThrowExceptionWhenNotInProgress() {
        // Given
        Delivery delivery = createBasicDelivery();
        BigDecimal payout = new BigDecimal("12.50");

        // When & Then
        assertThatThrownBy(() -> delivery.complete(payout))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot complete unless in progress.");
    }

    @Test
    void complete_shouldThrowExceptionWhenPayoutIsNull() {
        // Given
        Delivery delivery = createDeliveryInProgress();

        // When & Then
        assertThatThrownBy(() -> delivery.complete(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("betaling mag niet null zijn");
    }

    @Test
    void cancel_shouldUpdateStatusToOpenWhenNotReady() {
        // Given
        Delivery delivery = createBasicDelivery();

        // When
        delivery.cancel();

        // Then
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.OPEN);
        assertThat(delivery.getOrderStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(delivery.getAssignedDriverId()).isNull();
    }

    @Test
    void cancel_shouldThrowExceptionWhenOrderStatusIsReady() {
        // Given
        Delivery delivery = createBasicDelivery();
        delivery.markAsReady();

        // When & Then
        assertThatThrownBy(delivery::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot cancel a delivery that is already made");
    }

    @Test
    void cancel_shouldNotChangeAssignedDriver() {
        // Given
        Delivery delivery = createBasicDelivery();
        DriverId driverId = new DriverId(UUID.randomUUID());
        delivery.claim(driverId);

        // When
        delivery.cancel();

        // Then
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.OPEN);
        assertThat(delivery.getAssignedDriverId()).isEqualTo(driverId);
    }

    @Test
    void fullDeliveryWorkflow_shouldTransitionThroughAllStatesCorrectly() {
        // Given
        OrderId orderId = new OrderId(UUID.randomUUID());
        Address pickupAddress = new Address("Restaurant St", "5", null, "1000", "Brussels", "Belgium");
        Address deliveryAddress = new Address("Customer St", "10", "B", "2000", "Antwerp", "Belgium");
        DriverId driverId = new DriverId(UUID.randomUUID());
        BigDecimal payout = new BigDecimal("18.75");

        // When
        Delivery delivery = Delivery.createNew(orderId, pickupAddress, deliveryAddress);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.OPEN);
        assertThat(delivery.getOrderStatus()).isEqualTo(OrderStatus.ACCEPTED);

        delivery.markAsReady();
        assertThat(delivery.getOrderStatus()).isEqualTo(OrderStatus.READY);

        delivery.claim(driverId);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CLAIMED);
        assertThat(delivery.getAssignedDriverId()).isEqualTo(driverId);

        delivery.start();
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.IN_PROGRESS);
        assertThat(delivery.getPickupTime()).isNotNull();

        delivery.complete(payout);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(delivery.getDeliveryTime()).isNotNull();
        assertThat(delivery.getPrice()).isEqualTo(payout);

        // Then
        assertThat(delivery.getOrderId()).isEqualTo(orderId);
        assertThat(delivery.getPickupAddress()).isEqualTo(pickupAddress);
        assertThat(delivery.getDeliveryAddress()).isEqualTo(deliveryAddress);
        assertThat(delivery.getAssignedDriverId()).isEqualTo(driverId);
    }

    @Test
    void deliveryWithBusNumber_shouldHandleBusNumberCorrectly() {
        // Given
        OrderId orderId = new OrderId(UUID.randomUUID());
        Address pickupAddress = new Address("Street", "123", "B", "1000", "City", "Country");
        Address deliveryAddress = new Address("Avenue", "456", "C", "2000", "Town", "Country");

        // When
        Delivery delivery = Delivery.createNew(orderId, pickupAddress, deliveryAddress);

        // Then
        assertThat(delivery.getPickupAddress().busNumber()).isEqualTo("B");
        assertThat(delivery.getDeliveryAddress().busNumber()).isEqualTo("C");
    }

    @Test
    void deliveryWithoutBusNumber_shouldHandleNullBusNumberCorrectly() {
        // Given
        OrderId orderId = new OrderId(UUID.randomUUID());
        Address pickupAddress = new Address("Street", "123", null, "1000", "City", "Country");
        Address deliveryAddress = new Address("Avenue", "456", null, "2000", "Town", "Country");

        // When
        Delivery delivery = Delivery.createNew(orderId, pickupAddress, deliveryAddress);

        // Then
        assertThat(delivery.getPickupAddress().busNumber()).isNull();
        assertThat(delivery.getDeliveryAddress().busNumber()).isNull();
    }

    private Delivery createBasicDelivery() {
        OrderId orderId = new OrderId(UUID.randomUUID());
        Address pickupAddress = new Address("Pickup St", "1", null, "1000", "Brussels", "Belgium");
        Address deliveryAddress = new Address("Delivery St", "2", "A", "2000", "Antwerp", "Belgium");
        return Delivery.createNew(orderId, pickupAddress, deliveryAddress);
    }

    private Delivery createDeliveryInProgress() {
        Delivery delivery = createBasicDelivery();
        DriverId driverId = new DriverId(UUID.randomUUID());
        delivery.claim(driverId);
        delivery.markAsReady();
        delivery.start();
        return delivery;
    }
}