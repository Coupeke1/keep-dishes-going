package be.kdg.sa.backend.domain.driver;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.OrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DriverTest {

    private DriverId driverId;
    private Address address;
    private OrderId activeOrderId;
    private Driver basicDriver;
    private Driver driverWithActiveDelivery;

    @BeforeEach
    void setUp() {
        driverId = new DriverId(UUID.randomUUID());
        address = new Address("Main St", "123", "A", "1000", "Brussels", "BE");
        activeOrderId = new OrderId(UUID.randomUUID());

        basicDriver = new Driver(driverId, "John Doe", "john@example.com", "+3212345678", "BE123456789");
        driverWithActiveDelivery = new Driver(driverId, "John Doe", "john@example.com",
                "+3212345678", "BE123456789", address, activeOrderId);
    }

    @Test
    @DisplayName("Driver creation with minimal constructor should set basic fields correctly")
    void createDriverWithMinimalConstructor_ShouldSetBasicFields() {
        // Assert
        assertEquals(driverId, basicDriver.getId());
        assertEquals("John Doe", basicDriver.getName());
        assertEquals("john@example.com", basicDriver.getEmail());
        assertEquals("+3212345678", basicDriver.getPhoneNumber());
        assertEquals("BE123456789", basicDriver.getAccountNumber());
        assertNull(basicDriver.getAddress());
        assertNull(basicDriver.getActiveDeliveryId());
    }

    @Test
    @DisplayName("Driver creation with full constructor should set all fields correctly")
    void createDriverWithFullConstructor_ShouldSetAllFields() {
        // Assert
        assertEquals(driverId, driverWithActiveDelivery.getId());
        assertEquals("John Doe", driverWithActiveDelivery.getName());
        assertEquals("john@example.com", driverWithActiveDelivery.getEmail());
        assertEquals("+3212345678", driverWithActiveDelivery.getPhoneNumber());
        assertEquals("BE123456789", driverWithActiveDelivery.getAccountNumber());
        assertEquals(address, driverWithActiveDelivery.getAddress());
        assertEquals(activeOrderId, driverWithActiveDelivery.getActiveDeliveryId());
    }

    @Test
    @DisplayName("hasActiveDelivery should return false when no active delivery")
    void hasActiveDelivery_WhenNoActiveDelivery_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(basicDriver.hasActiveDelivery());
    }

    @Test
    @DisplayName("hasActiveDelivery should return true when has active delivery")
    void hasActiveDelivery_WhenHasActiveDelivery_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(driverWithActiveDelivery.hasActiveDelivery());
    }

    @Test
    @DisplayName("markAsAssigned should set active delivery when driver has no active delivery")
    void markAsAssigned_WhenNoActiveDelivery_ShouldSetActiveDelivery() {
        // Arrange
        OrderId newOrderId = new OrderId(UUID.randomUUID());

        // Act
        basicDriver.markAsAssigned(newOrderId);

        // Assert
        assertEquals(newOrderId, basicDriver.getActiveDeliveryId());
        assertTrue(basicDriver.hasActiveDelivery());
    }

    @Test
    @DisplayName("markAsAssigned should throw exception when driver already has active delivery")
    void markAsAssigned_WhenHasActiveDelivery_ShouldThrowException() {
        // Arrange
        OrderId newOrderId = new OrderId(UUID.randomUUID());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> driverWithActiveDelivery.markAsAssigned(newOrderId));

        assertEquals("Driver already has an active delivery.", exception.getMessage());
        assertEquals(activeOrderId, driverWithActiveDelivery.getActiveDeliveryId());
    }

    @Test
    @DisplayName("completeDelivery should clear active delivery when driver has active delivery")
    void completeDelivery_WhenHasActiveDelivery_ShouldClearActiveDelivery() {
        // Act
        driverWithActiveDelivery.completeDelivery();

        // Assert
        assertNull(driverWithActiveDelivery.getActiveDeliveryId());
        assertFalse(driverWithActiveDelivery.hasActiveDelivery());
    }

    @Test
    @DisplayName("completeDelivery should throw exception when driver has no active delivery")
    void completeDelivery_WhenNoActiveDelivery_ShouldThrowException() {
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> basicDriver.completeDelivery());

        assertEquals("No active delivery to complete.", exception.getMessage());
    }

    @Test
    @DisplayName("markDeliveryCancelled should clear active delivery when driver has active delivery")
    void markDeliveryCancelled_WhenHasActiveDelivery_ShouldClearActiveDelivery() {
        // Act
        driverWithActiveDelivery.markDeliveryCancelled();

        // Assert
        assertNull(driverWithActiveDelivery.getActiveDeliveryId());
        assertFalse(driverWithActiveDelivery.hasActiveDelivery());
    }

    @Test
    @DisplayName("markDeliveryCancelled should throw exception when driver has no active delivery")
    void markDeliveryCancelled_WhenNoActiveDelivery_ShouldThrowException() {
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> basicDriver.markDeliveryCancelled());

        assertEquals("No active delivery to cancel.", exception.getMessage());
    }

    @Test
    @DisplayName("setAddress should update driver address")
    void setAddress_ShouldUpdateAddress() {
        // Arrange
        Address newAddress = new Address("New St", "456", "B", "2000", "Antwerp", "BE");

        // Act
        basicDriver.setAddress(newAddress);

        // Assert
        assertEquals(newAddress, basicDriver.getAddress());
    }

    @Test
    @DisplayName("Driver with different ID should not be considered equal")
    void equals_WithDifferentDriverId_ShouldNotBeEqual() {
        // Arrange
        DriverId differentDriverId = new DriverId(UUID.randomUUID());
        Driver differentDriver = new Driver(differentDriverId, "John Doe", "john@example.com",
                "+3212345678", "BE123456789", address, activeOrderId);

        // Assert
        assertNotEquals(driverWithActiveDelivery, differentDriver);
    }

    @Test
    @DisplayName("Setting address to null should work")
    void setAddress_ToNull_ShouldWork() {
        // Act
        driverWithActiveDelivery.setAddress(null);

        // Assert
        assertNull(driverWithActiveDelivery.getAddress());
    }

    @Test
    @DisplayName("Driver state transitions work correctly")
    void driverStateTransitions_ShouldWorkCorrectly() {
        // Arrange
        Driver driver = new Driver(driverId, "Test Driver", "test@example.com",
                "+3212345678", "BE123456789");
        OrderId orderId = new OrderId(UUID.randomUUID());

        // Act & Assert
        assertFalse(driver.hasActiveDelivery());

        driver.markAsAssigned(orderId);
        assertTrue(driver.hasActiveDelivery());
        assertEquals(orderId, driver.getActiveDeliveryId());

        driver.completeDelivery();
        assertFalse(driver.hasActiveDelivery());
        assertNull(driver.getActiveDeliveryId());

        OrderId newOrderId = new OrderId(UUID.randomUUID());
        driver.markAsAssigned(newOrderId);
        assertTrue(driver.hasActiveDelivery());

        driver.markDeliveryCancelled();
        assertFalse(driver.hasActiveDelivery());
    }
}