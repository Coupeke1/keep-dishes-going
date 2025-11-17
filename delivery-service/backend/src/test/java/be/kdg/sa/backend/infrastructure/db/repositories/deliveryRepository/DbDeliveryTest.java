package be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.domain.delivery.DeliveryStatus;
import be.kdg.sa.backend.domain.delivery.OrderId;
import be.kdg.sa.backend.domain.delivery.OrderStatus;
import be.kdg.sa.backend.domain.driver.DriverId;
import be.kdg.sa.backend.infrastructure.db.repositories.EntityNotFoundException;
import be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.jpa.JpaDeliveryEntity;
import be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.jpa.JpaDeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbDeliveryTest {

    @Mock
    private JpaDeliveryRepository jpaDeliveryRepository;

    private DbDelivery dbDelivery;
    private OrderId orderId;
    private UUID orderUuid;
    private Delivery delivery;

    @BeforeEach
    void setUp() {
        dbDelivery = new DbDelivery(jpaDeliveryRepository);
        orderUuid = UUID.randomUUID();
        orderId = new OrderId(orderUuid);

        Address pickupAddress = new Address("Street 1", "3", null, "City1", "1000", "1");
        Address deliveryAddress = new Address("Street 2", "5", null, "City2", "2000", "2");

        delivery = Delivery.createNew(orderId, pickupAddress, deliveryAddress);
    }

    @Test
    void findById_WithExistingDelivery_ShouldReturnDelivery() {
        JpaDeliveryEntity jpaDeliveryEntity = mock(JpaDeliveryEntity.class);
        when(jpaDeliveryRepository.findById(orderUuid)).thenReturn(Optional.of(jpaDeliveryEntity));
        when(jpaDeliveryEntity.toDomain()).thenReturn(delivery);

        Delivery result = dbDelivery.findById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        verify(jpaDeliveryRepository).findById(orderUuid);
        verify(jpaDeliveryEntity).toDomain();
    }

    @Test
    void findById_WithNonExistingDelivery_ShouldThrowEntityNotFoundException() {
        when(jpaDeliveryRepository.findById(orderUuid)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> dbDelivery.findById(orderId));

        assertEquals("Entity not found: Delivery not found: " + orderUuid, exception.getMessage());
        verify(jpaDeliveryRepository).findById(orderUuid);
    }

    @Test
    void findById_WithNullOrderId_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> dbDelivery.findById(null));
    }

    @Test
    void save_WithValidDelivery_ShouldSaveEntity() {
        when(jpaDeliveryRepository.save(any(JpaDeliveryEntity.class))).thenReturn(mock(JpaDeliveryEntity.class));

        dbDelivery.save(delivery);

        verify(jpaDeliveryRepository).save(any(JpaDeliveryEntity.class));
    }

    @Test
    void save_WithNullDelivery_ShouldThrowNullPointerException() {
        assertThrows(IllegalArgumentException.class, () -> dbDelivery.save(null));
    }

    @Test
    void findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc_WithValidParameters_ShouldReturnEntities() {
        UUID driverUuid = UUID.randomUUID();
        DriverId driverId = new DriverId(driverUuid);
        DeliveryStatus status = DeliveryStatus.IN_PROGRESS;

        delivery.claim(driverId);
        delivery.markAsReady();
        delivery.start();

        List<JpaDeliveryEntity> expectedEntities = List.of(mock(JpaDeliveryEntity.class));
        when(jpaDeliveryRepository.findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(driverUuid, status))
                .thenReturn(expectedEntities);

        List<JpaDeliveryEntity> result = dbDelivery.findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(driverUuid, status);

        assertNotNull(result);
        assertEquals(expectedEntities, result);
        verify(jpaDeliveryRepository).findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(driverUuid, status);
    }

    @Test
    void findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc_WithNullDriverId_ShouldHandleNull() {
        DeliveryStatus status = DeliveryStatus.OPEN;
        List<JpaDeliveryEntity> expectedEntities = List.of();
        when(jpaDeliveryRepository.findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(null, status))
                .thenReturn(expectedEntities);

        List<JpaDeliveryEntity> result = dbDelivery.findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(null, status);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jpaDeliveryRepository).findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(null, status);
    }

    @Test
    void findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc_WithNullStatus_ShouldHandleNull() {
        UUID driverUuid = UUID.randomUUID();
        List<JpaDeliveryEntity> expectedEntities = List.of();
        when(jpaDeliveryRepository.findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(driverUuid, null))
                .thenReturn(expectedEntities);

        List<JpaDeliveryEntity> result = dbDelivery.findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(driverUuid, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jpaDeliveryRepository).findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(driverUuid, null);
    }

    @Test
    void save_ShouldConvertDomainToEntityAndSave() {
        UUID driverUuid = UUID.randomUUID();
        DriverId driverId = new DriverId(driverUuid);
        BigDecimal price = new BigDecimal("15.50");

        delivery.claim(driverId);
        delivery.markAsReady();
        delivery.start();
        delivery.complete(price);

        when(jpaDeliveryRepository.save(any(JpaDeliveryEntity.class))).thenReturn(mock(JpaDeliveryEntity.class));

        dbDelivery.save(delivery);

        verify(jpaDeliveryRepository).save(any(JpaDeliveryEntity.class));
    }

    @Test
    void findById_ShouldHandleEntityConversionCorrectly() {
        UUID driverUuid = UUID.randomUUID();
        DriverId driverId = new DriverId(driverUuid);
        LocalDateTime pickupTime = LocalDateTime.now().minusHours(1);
        LocalDateTime deliveryTime = LocalDateTime.now();
        BigDecimal price = new BigDecimal("15.50");

        Address pickupAddress = new Address("Pickup St", "10", "5C", "Pickup City", "1000", "10");
        Address deliveryAddress = new Address("Delivery St", "60", "62A", "Delivery City", "2000", "20");

        Delivery complexDelivery = Delivery.reconstruct(
                orderId,
                DeliveryStatus.DELIVERED,
                OrderStatus.READY,
                pickupAddress,
                deliveryAddress,
                pickupTime,
                deliveryTime,
                driverId,
                price
        );

        JpaDeliveryEntity jpaDeliveryEntity = mock(JpaDeliveryEntity.class);
        when(jpaDeliveryRepository.findById(orderUuid)).thenReturn(Optional.of(jpaDeliveryEntity));
        when(jpaDeliveryEntity.toDomain()).thenReturn(complexDelivery);

        Delivery result = dbDelivery.findById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getOrderId());
        assertEquals(DeliveryStatus.DELIVERED, result.getStatus());
        assertEquals(OrderStatus.READY, result.getOrderStatus());
        assertEquals(price, result.getPrice());
        assertEquals(driverId, result.getAssignedDriverId());
        verify(jpaDeliveryRepository).findById(orderUuid);
    }

    @Test
    void findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc_WithMultipleResults_ShouldReturnAll() {
        UUID driverUuid = UUID.randomUUID();
        DeliveryStatus status = DeliveryStatus.IN_PROGRESS;

        JpaDeliveryEntity entity1 = mock(JpaDeliveryEntity.class);
        JpaDeliveryEntity entity2 = mock(JpaDeliveryEntity.class);
        List<JpaDeliveryEntity> expectedEntities = List.of(entity1, entity2);

        when(jpaDeliveryRepository.findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(driverUuid, status))
                .thenReturn(expectedEntities);

        List<JpaDeliveryEntity> result = dbDelivery.findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(driverUuid, status);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedEntities, result);
        verify(jpaDeliveryRepository).findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(driverUuid, status);
    }

    @Test
    void constructor_WithNullRepository_ShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DbDelivery(null));
    }
}