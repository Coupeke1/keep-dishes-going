package be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.jpa;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.domain.delivery.DeliveryStatus;
import be.kdg.sa.backend.domain.delivery.OrderId;
import be.kdg.sa.backend.domain.delivery.OrderStatus;
import be.kdg.sa.backend.domain.driver.DriverId;
import be.kdg.sa.backend.infrastructure.db.repositories.JpaAddressEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JpaDeliveryEntityTest {

    @Test
    void fromDomain_shouldConvertDeliveryToJpaEntity() {
        // Given
        OrderId orderId = new OrderId(UUID.randomUUID());
        Address pickupAddress = new Address("Pickup St", "1", "A", "1000", "Brussels", "Belgium");
        Address deliveryAddress = new Address("Delivery St", "2", "B", "2000", "Antwerp", "Belgium");
        LocalDateTime pickupTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime deliveryTime = LocalDateTime.of(2024, 1, 1, 10, 30);
        DriverId driverId = new DriverId(UUID.randomUUID());
        BigDecimal price = new BigDecimal("15.50");

        Delivery delivery = Delivery.reconstruct(
                orderId, DeliveryStatus.DELIVERED, OrderStatus.DELIVERED,
                pickupAddress, deliveryAddress, pickupTime, deliveryTime, driverId, price
        );

        // When
        JpaDeliveryEntity entity = JpaDeliveryEntity.fromDomain(delivery);

        // Then
        assertThat(entity.getOrderId()).isEqualTo(orderId.id());
        assertThat(entity.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(entity.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(entity.getPickupTime()).isEqualTo(pickupTime);
        assertThat(entity.getDeliveryTime()).isEqualTo(deliveryTime);
        assertThat(entity.getPrice()).isEqualTo(price);
        assertThat(entity.getAssignedDriverId()).isEqualTo(driverId.id());

        // Verify addresses are converted
        assertThat(entity.getPickupAddress()).isNotNull();
        assertThat(entity.getPickupAddress().getStreet()).isEqualTo("Pickup St");
        assertThat(entity.getPickupAddress().getBusNumber()).isEqualTo("A");

        assertThat(entity.getDeliveryAddress()).isNotNull();
        assertThat(entity.getDeliveryAddress().getStreet()).isEqualTo("Delivery St");
        assertThat(entity.getDeliveryAddress().getBusNumber()).isEqualTo("B");
    }

    @Test
    void fromDomain_shouldReturnNullWhenDeliveryIsNull() {
        // When
        JpaDeliveryEntity entity = JpaDeliveryEntity.fromDomain(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    void fromDomain_shouldHandleNullOrderId() {
        // Given
        Address pickupAddress = new Address("Pickup St", "1", null, "1000", "Brussels", "Belgium");
        Address deliveryAddress = new Address("Delivery St", "2", null, "2000", "Antwerp", "Belgium");

        Delivery delivery = Delivery.reconstruct(
                null, DeliveryStatus.OPEN, OrderStatus.ACCEPTED,
                pickupAddress, deliveryAddress, null, null, null, null
        );

        // When
        JpaDeliveryEntity entity = JpaDeliveryEntity.fromDomain(delivery);

        // Then - should generate a random UUID when orderId is null
        assertThat(entity.getOrderId()).isNotNull();
        assertThat(entity.getStatus()).isEqualTo(DeliveryStatus.OPEN);
        assertThat(entity.getOrderStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    void fromDomain_shouldHandleNullDriverId() {
        // Given
        OrderId orderId = new OrderId(UUID.randomUUID());
        Address pickupAddress = new Address("Pickup St", "1", null, "1000", "Brussels", "Belgium");
        Address deliveryAddress = new Address("Delivery St", "2", null, "2000", "Antwerp", "Belgium");

        Delivery delivery = Delivery.reconstruct(
                orderId, DeliveryStatus.OPEN, OrderStatus.ACCEPTED,
                pickupAddress, deliveryAddress, null, null, null, null
        );

        // When
        JpaDeliveryEntity entity = JpaDeliveryEntity.fromDomain(delivery);

        // Then
        assertThat(entity.getAssignedDriverId()).isNull();
    }

    @Test
    void fromDomain_shouldHandleNullAddresses() {
        // Given
        OrderId orderId = new OrderId(UUID.randomUUID());

        Delivery delivery = Delivery.reconstruct(
                orderId, DeliveryStatus.OPEN, OrderStatus.ACCEPTED,
                null, null, null, null, null, null
        );

        // When
        JpaDeliveryEntity entity = JpaDeliveryEntity.fromDomain(delivery);

        // Then
        assertThat(entity.getPickupAddress()).isNull();
        assertThat(entity.getDeliveryAddress()).isNull();
    }

    @Test
    void toDomain_shouldConvertJpaEntityToDelivery() {
        // Given
        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        LocalDateTime pickupTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime deliveryTime = LocalDateTime.of(2024, 1, 1, 10, 30);
        BigDecimal price = new BigDecimal("12.50");

        JpaAddressEntity pickupAddress = new JpaAddressEntity(
                UUID.randomUUID(), "Pickup St", "1", "A", "1000", "Brussels", "Belgium"
        );
        JpaAddressEntity deliveryAddress = new JpaAddressEntity(
                UUID.randomUUID(), "Delivery St", "2", "B", "2000", "Antwerp", "Belgium"
        );

        JpaDeliveryEntity entity = new JpaDeliveryEntity(
                orderId, DeliveryStatus.DELIVERED, OrderStatus.DELIVERED,
                pickupAddress, deliveryAddress, pickupTime, deliveryTime, price, driverId
        );

        // When
        Delivery delivery = entity.toDomain();

        // Then
        assertThat(delivery.getOrderId().id()).isEqualTo(orderId);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(delivery.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(delivery.getPickupTime()).isEqualTo(pickupTime);
        assertThat(delivery.getDeliveryTime()).isEqualTo(deliveryTime);
        assertThat(delivery.getPrice()).isEqualTo(price);
        assertThat(delivery.getAssignedDriverId().id()).isEqualTo(driverId);

        // Verify addresses are converted
        assertThat(delivery.getPickupAddress()).isNotNull();
        assertThat(delivery.getPickupAddress().street()).isEqualTo("Pickup St");
        assertThat(delivery.getPickupAddress().busNumber()).isEqualTo("A");

        assertThat(delivery.getDeliveryAddress()).isNotNull();
        assertThat(delivery.getDeliveryAddress().street()).isEqualTo("Delivery St");
        assertThat(delivery.getDeliveryAddress().busNumber()).isEqualTo("B");
    }

    @Test
    void toDomain_shouldHandleNullAddresses() {
        // Given
        UUID orderId = UUID.randomUUID();

        JpaDeliveryEntity entity = new JpaDeliveryEntity(
                orderId, DeliveryStatus.OPEN, OrderStatus.ACCEPTED,
                null, null, null, null, null, null
        );

        // When
        Delivery delivery = entity.toDomain();

        // Then
        assertThat(delivery.getPickupAddress()).isNull();
        assertThat(delivery.getDeliveryAddress()).isNull();
        assertThat(delivery.getAssignedDriverId()).isNull();
        assertThat(delivery.getPrice()).isNull();
    }

    @Test
    void toDomain_shouldHandleNullDriverId() {
        // Given
        UUID orderId = UUID.randomUUID();
        JpaAddressEntity pickupAddress = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", null, "1000", "City", "Country"
        );
        JpaAddressEntity deliveryAddress = new JpaAddressEntity(
                UUID.randomUUID(), "Avenue", "2", null, "2000", "Town", "Country"
        );

        JpaDeliveryEntity entity = new JpaDeliveryEntity(
                orderId, DeliveryStatus.OPEN, OrderStatus.ACCEPTED,
                pickupAddress, deliveryAddress, null, null, null, null
        );

        // When
        Delivery delivery = entity.toDomain();

        // Then
        assertThat(delivery.getAssignedDriverId()).isNull();
    }

    @Test
    void updateFromDomain_shouldUpdateEntityFromDelivery() {
        // Given
        UUID orderId = UUID.randomUUID();
        JpaAddressEntity existingPickupAddress = new JpaAddressEntity(
                UUID.randomUUID(), "Old St", "1", "A", "1000", "Old City", "Country"
        );
        JpaAddressEntity existingDeliveryAddress = new JpaAddressEntity(
                UUID.randomUUID(), "Old Ave", "2", "B", "2000", "Old Town", "Country"
        );

        JpaDeliveryEntity entity = new JpaDeliveryEntity(
                orderId, DeliveryStatus.OPEN, OrderStatus.ACCEPTED,
                existingPickupAddress, existingDeliveryAddress, null, null, null, null
        );

        // Updated delivery data
        Address newPickupAddress = new Address("New St", "10", "C", "3000", "New City", "Country");
        Address newDeliveryAddress = new Address("New Ave", "20", "D", "4000", "New Town", "Country");
        LocalDateTime pickupTime = LocalDateTime.of(2024, 1, 1, 11, 0);
        LocalDateTime deliveryTime = LocalDateTime.of(2024, 1, 1, 11, 45);
        DriverId driverId = new DriverId(UUID.randomUUID());
        BigDecimal price = new BigDecimal("18.75");

        Delivery updatedDelivery = Delivery.reconstruct(
                new OrderId(orderId), DeliveryStatus.DELIVERED, OrderStatus.DELIVERED,
                newPickupAddress, newDeliveryAddress, pickupTime, deliveryTime, driverId, price
        );

        // When
        entity.updateFromDomain(updatedDelivery);

        // Then
        assertThat(entity.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(entity.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(entity.getPickupTime()).isEqualTo(pickupTime);
        assertThat(entity.getDeliveryTime()).isEqualTo(deliveryTime);
        assertThat(entity.getPrice()).isEqualTo(price);
        assertThat(entity.getAssignedDriverId()).isEqualTo(driverId.id());

        // Verify addresses are updated (not replaced)
        assertThat(entity.getPickupAddress()).isSameAs(existingPickupAddress);
        assertThat(entity.getPickupAddress().getStreet()).isEqualTo("New St");
        assertThat(entity.getPickupAddress().getHouseNumber()).isEqualTo("10");
        assertThat(entity.getPickupAddress().getBusNumber()).isEqualTo("C");
        assertThat(entity.getPickupAddress().getPostalCode()).isEqualTo("3000");
        assertThat(entity.getPickupAddress().getCity()).isEqualTo("New City");

        assertThat(entity.getDeliveryAddress()).isSameAs(existingDeliveryAddress);
        assertThat(entity.getDeliveryAddress().getStreet()).isEqualTo("New Ave");
        assertThat(entity.getDeliveryAddress().getHouseNumber()).isEqualTo("20");
        assertThat(entity.getDeliveryAddress().getBusNumber()).isEqualTo("D");
        assertThat(entity.getDeliveryAddress().getPostalCode()).isEqualTo("4000");
        assertThat(entity.getDeliveryAddress().getCity()).isEqualTo("New Town");
    }

    @Test
    void updateFromDomain_shouldCreateNewAddressesWhenNull() {
        // Given
        UUID orderId = UUID.randomUUID();
        JpaDeliveryEntity entity = new JpaDeliveryEntity(
                orderId, DeliveryStatus.OPEN, OrderStatus.ACCEPTED,
                null, null, null, null, null, null
        );

        Address newPickupAddress = new Address("New St", "1", null, "1000", "City", "Country");
        Address newDeliveryAddress = new Address("New Ave", "2", null, "2000", "Town", "Country");

        Delivery updatedDelivery = Delivery.reconstruct(
                new OrderId(orderId), DeliveryStatus.CLAIMED, OrderStatus.READY,
                newPickupAddress, newDeliveryAddress, null, null, null, null
        );

        // When
        entity.updateFromDomain(updatedDelivery);

        // Then
        assertThat(entity.getPickupAddress()).isNotNull();
        assertThat(entity.getPickupAddress().getStreet()).isEqualTo("New St");
        assertThat(entity.getDeliveryAddress()).isNotNull();
        assertThat(entity.getDeliveryAddress().getStreet()).isEqualTo("New Ave");
    }

    @Test
    void updateFromDomain_shouldHandleNullDelivery() {
        // Given
        UUID orderId = UUID.randomUUID();
        JpaAddressEntity pickupAddress = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", null, "1000", "City", "Country"
        );
        JpaAddressEntity deliveryAddress = new JpaAddressEntity(
                UUID.randomUUID(), "Avenue", "2", null, "2000", "Town", "Country"
        );

        JpaDeliveryEntity entity = new JpaDeliveryEntity(
                orderId, DeliveryStatus.OPEN, OrderStatus.ACCEPTED,
                pickupAddress, deliveryAddress, null, null, null, null
        );

        // When - should not throw exception
        entity.updateFromDomain(null);

        // Then - entity should remain unchanged
        assertThat(entity.getStatus()).isEqualTo(DeliveryStatus.OPEN);
        assertThat(entity.getOrderStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(entity.getPickupAddress()).isSameAs(pickupAddress);
        assertThat(entity.getDeliveryAddress()).isSameAs(deliveryAddress);
    }

    @Test
    void updateFromDomain_shouldHandleNullAddressesInDelivery() {
        // Given
        UUID orderId = UUID.randomUUID();
        JpaAddressEntity existingPickupAddress = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", null, "1000", "City", "Country"
        );
        JpaAddressEntity existingDeliveryAddress = new JpaAddressEntity(
                UUID.randomUUID(), "Avenue", "2", null, "2000", "Town", "Country"
        );

        JpaDeliveryEntity entity = new JpaDeliveryEntity(
                orderId, DeliveryStatus.OPEN, OrderStatus.ACCEPTED,
                existingPickupAddress, existingDeliveryAddress, null, null, null, null
        );

        Delivery deliveryWithNullAddresses = Delivery.reconstruct(
                new OrderId(orderId), DeliveryStatus.CLAIMED, OrderStatus.READY,
                null, null, null, null, null, null
        );

        // When
        entity.updateFromDomain(deliveryWithNullAddresses);

        // Then - addresses should remain unchanged
        assertThat(entity.getStatus()).isEqualTo(DeliveryStatus.CLAIMED);
        assertThat(entity.getOrderStatus()).isEqualTo(OrderStatus.READY);
        assertThat(entity.getPickupAddress()).isSameAs(existingPickupAddress);
        assertThat(entity.getDeliveryAddress()).isSameAs(existingDeliveryAddress);
    }

    @Test
    void updateFromDomain_shouldHandleNullDriverIdInDelivery() {
        // Given
        UUID orderId = UUID.randomUUID();
        UUID existingDriverId = UUID.randomUUID();

        JpaDeliveryEntity entity = new JpaDeliveryEntity(
                orderId, DeliveryStatus.CLAIMED, OrderStatus.READY,
                null, null, null, null, null, existingDriverId
        );

        Delivery deliveryWithNullDriver = Delivery.reconstruct(
                new OrderId(orderId), DeliveryStatus.IN_PROGRESS, OrderStatus.READY,
                null, null, null, null, null, null
        );

        // When
        entity.updateFromDomain(deliveryWithNullDriver);

        // Then - driver ID should be set to null
        assertThat(entity.getAssignedDriverId()).isNull();
    }

    @Test
    void constructor_shouldCreateEntityWithAllFields() {
        // Given
        UUID orderId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        LocalDateTime pickupTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime deliveryTime = LocalDateTime.of(2024, 1, 1, 10, 30);
        BigDecimal price = new BigDecimal("15.00");

        JpaAddressEntity pickupAddress = new JpaAddressEntity(
                UUID.randomUUID(), "Street", "1", null, "1000", "City", "Country"
        );
        JpaAddressEntity deliveryAddress = new JpaAddressEntity(
                UUID.randomUUID(), "Avenue", "2", null, "2000", "Town", "Country"
        );

        // When
        JpaDeliveryEntity entity = new JpaDeliveryEntity(
                orderId, DeliveryStatus.DELIVERED, OrderStatus.DELIVERED,
                pickupAddress, deliveryAddress, pickupTime, deliveryTime, price, driverId
        );

        // Then
        assertThat(entity.getOrderId()).isEqualTo(orderId);
        assertThat(entity.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(entity.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(entity.getPickupAddress()).isEqualTo(pickupAddress);
        assertThat(entity.getDeliveryAddress()).isEqualTo(deliveryAddress);
        assertThat(entity.getPickupTime()).isEqualTo(pickupTime);
        assertThat(entity.getDeliveryTime()).isEqualTo(deliveryTime);
        assertThat(entity.getPrice()).isEqualTo(price);
        assertThat(entity.getAssignedDriverId()).isEqualTo(driverId);
    }

    @Test
    void defaultConstructor_shouldExistForJpa() {
        // When
        JpaDeliveryEntity entity = new JpaDeliveryEntity();

        // Then
        assertThat(entity).isNotNull();
    }
}