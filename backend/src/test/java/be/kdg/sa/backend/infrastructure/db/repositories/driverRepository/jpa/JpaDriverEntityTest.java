package be.kdg.sa.backend.infrastructure.db.repositories.driverRepository.jpa;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.OrderId;
import be.kdg.sa.backend.domain.driver.Driver;
import be.kdg.sa.backend.domain.driver.DriverId;
import be.kdg.sa.backend.infrastructure.db.repositories.JpaAddressEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JpaDriverEntityTest {

    @Test
    void fromDomain_WithValidDriver_ShouldCreateEntity() {
        DriverId driverId = new DriverId(UUID.randomUUID());
        OrderId orderId = new OrderId(UUID.randomUUID());
        Address address = new Address("Main St", "123", "A", "1000", "Brussels", "Belgium");
        Driver driver = new Driver(driverId, "John Doe", "john@example.com", "123456789", "BE123456789", address, orderId);

        JpaDriverEntity entity = JpaDriverEntity.fromDomain(driver);

        assertNotNull(entity);
        assertEquals(driverId.id(), entity.getId());
        assertEquals("John Doe", entity.getName());
        assertEquals("john@example.com", entity.getEmail());
        assertEquals("123456789", entity.getPhoneNumber());
        assertEquals("BE123456789", entity.getAccountNumber());
        assertEquals(orderId.id(), entity.getActiveDeliveryId());
        assertNotNull(entity.getAddress());
    }

    @Test
    void fromDomain_WithDriverWithoutActiveDelivery_ShouldCreateEntity() {
        DriverId driverId = new DriverId(UUID.randomUUID());
        Address address = new Address("Main St", "123", null, "1000", "Brussels", "Belgium");
        Driver driver = new Driver(driverId, "John Doe", "john@example.com", "123456789", "BE123456789", address, null);

        JpaDriverEntity entity = JpaDriverEntity.fromDomain(driver);

        assertNotNull(entity);
        assertNull(entity.getActiveDeliveryId());
        assertEquals("John Doe", entity.getName());
        assertEquals("john@example.com", entity.getEmail());
    }

    @Test
    void fromDomain_WithDriverWithoutAddress_ShouldCreateEntity() {
        DriverId driverId = new DriverId(UUID.randomUUID());
        Driver driver = new Driver(driverId, "John Doe", "john@example.com", "123456789", "BE123456789");

        JpaDriverEntity entity = JpaDriverEntity.fromDomain(driver);

        assertNotNull(entity);
        assertNull(entity.getAddress());
        assertEquals("John Doe", entity.getName());
        assertEquals("BE123456789", entity.getAccountNumber());
    }

    @Test
    void fromDomain_WithNullDriver_ShouldReturnNull() {
        JpaDriverEntity entity = JpaDriverEntity.fromDomain(null);

        assertNull(entity);
    }

    @Test
    void fromDomain_WithDriverWithoutId_ShouldGenerateNewId() {
        Address address = new Address("Main St", "123", null, "1000", "Brussels", "Belgium");
        Driver driver = new Driver(null, "John Doe", "john@example.com", "123456789", "BE123456789", address, null);

        JpaDriverEntity entity = JpaDriverEntity.fromDomain(driver);

        assertNotNull(entity);
        assertNotNull(entity.getId());
        assertEquals("John Doe", entity.getName());
    }

    @Test
    void updateFromDomain_WithValidDriver_ShouldUpdateEntity() {
        UUID driverUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();
        JpaDriverEntity entity = new JpaDriverEntity(
                driverUuid, "Old Name", "old@example.com", "111111111", "OLD123",
                null, null
        );

        DriverId driverId = new DriverId(driverUuid);
        OrderId orderId = new OrderId(orderUuid);
        Address newAddress = new Address("New St", "456", "B", "2000", "Antwerp", "Belgium");
        Driver updatedDriver = new Driver(driverId, "New Name", "new@example.com", "999999999", "NEW456", newAddress, orderId);

        entity.updateFromDomain(updatedDriver);

        assertEquals("New Name", entity.getName());
        assertEquals("new@example.com", entity.getEmail());
        assertEquals("999999999", entity.getPhoneNumber());
        assertEquals("NEW456", entity.getAccountNumber());
        assertEquals(orderUuid, entity.getActiveDeliveryId());
        assertNotNull(entity.getAddress());
    }

    @Test
    void updateFromDomain_WithNullDriver_ShouldNotUpdate() {
        UUID driverUuid = UUID.randomUUID();
        JpaDriverEntity entity = new JpaDriverEntity(
                driverUuid, "Original Name", "original@example.com", "111111111", "ORIGINAL",
                null, null
        );

        entity.updateFromDomain(null);

        assertEquals("Original Name", entity.getName());
        assertEquals("original@example.com", entity.getEmail());
        assertEquals("111111111", entity.getPhoneNumber());
        assertEquals("ORIGINAL", entity.getAccountNumber());
    }

    @Test
    void updateFromDomain_WithNullActiveDelivery_ShouldSetNull() {
        UUID driverUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();
        JpaDriverEntity entity = new JpaDriverEntity(
                driverUuid, "John Doe", "john@example.com", "123456789", "BE123",
                null, orderUuid
        );

        Driver driver = new Driver(new DriverId(driverUuid), "John Doe", "john@example.com", "123456789", "BE123", null, null);

        entity.updateFromDomain(driver);

        assertNull(entity.getActiveDeliveryId());
    }

    @Test
    void updateFromDomain_WithExistingAddress_ShouldUpdateAddress() {
        UUID driverUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();
        JpaAddressEntity existingAddress = mock(JpaAddressEntity.class);
        JpaDriverEntity entity = new JpaDriverEntity(
                driverUuid, "John Doe", "john@example.com", "123456789", "BE123",
                existingAddress, orderUuid
        );

        Address newAddress = new Address("Updated St", "789", null, "3000", "Ghent", "Belgium");
        Driver driver = new Driver(new DriverId(driverUuid), "John Doe", "john@example.com", "123456789", "BE123", newAddress, new OrderId(orderUuid));

        entity.updateFromDomain(driver);

        verify(existingAddress).updateFromDomain(newAddress);
    }

    @Test
    void updateFromDomain_WithNewAddress_ShouldCreateNewAddress() {
        UUID driverUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();
        JpaDriverEntity entity = new JpaDriverEntity(
                driverUuid, "John Doe", "john@example.com", "123456789", "BE123",
                null, orderUuid
        );

        Address newAddress = new Address("New St", "123", null, "1000", "Brussels", "Belgium");
        Driver driver = new Driver(new DriverId(driverUuid), "John Doe", "john@example.com", "123456789", "BE123", newAddress, new OrderId(orderUuid));

        entity.updateFromDomain(driver);

        assertNotNull(entity.getAddress());
    }

    @Test
    void updateFromDomain_WithNullAddress_ShouldSetAddressToNull() {
        UUID driverUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();
        JpaAddressEntity existingAddress = mock(JpaAddressEntity.class);
        JpaDriverEntity entity = new JpaDriverEntity(
                driverUuid, "John Doe", "john@example.com", "123456789", "BE123",
                existingAddress, orderUuid
        );

        Driver driver = new Driver(new DriverId(driverUuid), "John Doe", "john@example.com", "123456789", "BE123", null, new OrderId(orderUuid));

        entity.updateFromDomain(driver);

        assertNull(entity.getAddress());
    }

    @Test
    void toDomain_WithCompleteEntity_ShouldReturnDriver() {
        UUID driverUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();
        JpaAddressEntity addressEntity = new JpaAddressEntity(
                UUID.randomUUID(), "Main St", "123", null, "1000", "Brussels", "Belgium"
        );
        JpaDriverEntity entity = new JpaDriverEntity(
                driverUuid, "John Doe", "john@example.com", "123456789", "BE123456789",
                addressEntity, orderUuid
        );

        Driver driver = entity.toDomain();

        assertNotNull(driver);
        assertEquals(driverUuid, driver.getId().id());
        assertEquals("John Doe", driver.getName());
        assertEquals("john@example.com", driver.getEmail());
        assertEquals("123456789", driver.getPhoneNumber());
        assertEquals("BE123456789", driver.getAccountNumber());
        assertEquals(orderUuid, driver.getActiveDeliveryId().id());
        assertNotNull(driver.getAddress());
    }

    @Test
    void toDomain_WithoutActiveDelivery_ShouldReturnDriverWithNullActiveDelivery() {
        UUID driverUuid = UUID.randomUUID();
        JpaAddressEntity addressEntity = new JpaAddressEntity(
                UUID.randomUUID(), "Main St", "123", null, "1000", "Brussels", "Belgium"
        );
        JpaDriverEntity entity = new JpaDriverEntity(
                driverUuid, "John Doe", "john@example.com", "123456789", "BE123456789",
                addressEntity, null
        );

        Driver driver = entity.toDomain();

        assertNotNull(driver);
        assertNull(driver.getActiveDeliveryId());
        assertEquals("John Doe", driver.getName());
        assertNotNull(driver.getAddress());
    }

    @Test
    void toDomain_WithoutAddress_ShouldReturnDriverWithNullAddress() {
        UUID driverUuid = UUID.randomUUID();
        JpaDriverEntity entity = new JpaDriverEntity(
                driverUuid, "John Doe", "john@example.com", "123456789", "BE123456789",
                null, null
        );

        Driver driver = entity.toDomain();

        assertNotNull(driver);
        assertNull(driver.getAddress());
        assertEquals("John Doe", driver.getName());
        assertEquals("john@example.com", driver.getEmail());
    }

    @Test
    void constructor_WithAllParameters_ShouldCreateEntity() {
        UUID driverUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();
        JpaAddressEntity addressEntity = new JpaAddressEntity(
                UUID.randomUUID(), "Main St", "123", null, "1000", "Brussels", "Belgium"
        );

        JpaDriverEntity entity = new JpaDriverEntity(
                driverUuid, "John Doe", "john@example.com", "123456789", "BE123456789",
                addressEntity, orderUuid
        );

        assertNotNull(entity);
        assertEquals(driverUuid, entity.getId());
        assertEquals("John Doe", entity.getName());
        assertEquals("john@example.com", entity.getEmail());
        assertEquals("123456789", entity.getPhoneNumber());
        assertEquals("BE123456789", entity.getAccountNumber());
        assertEquals(addressEntity, entity.getAddress());
        assertEquals(orderUuid, entity.getActiveDeliveryId());
    }

    @Test
    void protectedNoArgConstructor_ShouldExist() {
        JpaDriverEntity entity = new JpaDriverEntity();

        assertNotNull(entity);
    }
}