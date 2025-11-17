package be.kdg.sa.backend.application.services.delivery;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.domain.delivery.DeliveryStatus;
import be.kdg.sa.backend.domain.delivery.OrderId;
import be.kdg.sa.backend.domain.delivery.OrderStatus;
import be.kdg.sa.backend.infrastructure.db.repositories.EntityNotFoundException;
import be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.DeliveryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    @Test
    void createDelivery_ShouldCreateAndSaveDelivery() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Address pickupAddress = new Address("Restaurant St", "1", null, "1000", "Brussels", "BE");
        Address deliveryAddress = new Address("Customer St", "2", "A", "1000", "Brussels", "BE");

        OrderId orderIdObj = new OrderId(orderId);

        // Act
        Delivery result = deliveryService.createDelivery(orderId, pickupAddress, deliveryAddress);

        // Assert
        assertNotNull(result);
        assertEquals(orderIdObj, result.getOrderId());
        assertEquals(DeliveryStatus.OPEN, result.getStatus());
        assertEquals(OrderStatus.ACCEPTED, result.getOrderStatus());
        assertEquals(pickupAddress, result.getPickupAddress());
        assertEquals(deliveryAddress, result.getDeliveryAddress());

        verify(deliveryRepository).save(any(Delivery.class));
    }

    @Test
    void getById_WithExistingDelivery_ShouldReturnDelivery() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderId orderIdObj = new OrderId(orderId);
        Address pickupAddress = new Address("Restaurant St", "1", null, "1000", "Brussels", "BE");
        Address deliveryAddress = new Address("Customer St", "2", "A", "1000", "Brussels", "BE");

        Delivery expectedDelivery = Delivery.createNew(orderIdObj, pickupAddress, deliveryAddress);
        when(deliveryRepository.findById(orderIdObj)).thenReturn(expectedDelivery);

        // Act
        Delivery result = deliveryService.getById(orderIdObj);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDelivery, result);
        verify(deliveryRepository).findById(orderIdObj);
    }

    @Test
    void getById_WithNonExistingDelivery_ShouldThrowException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderId orderIdObj = new OrderId(orderId);
        when(deliveryRepository.findById(orderIdObj)).thenThrow(new EntityNotFoundException("Delivery not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> deliveryService.getById(orderIdObj));
        verify(deliveryRepository).findById(orderIdObj);
    }

    @Test
    void deliveryReady_WithExistingDelivery_ShouldMarkAsReadyAndSave() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderId orderIdObj = new OrderId(orderId);
        Address pickupAddress = new Address("Restaurant St", "1", null, "1000", "Brussels", "BE");
        Address deliveryAddress = new Address("Customer St", "2", "A", "1000", "Brussels", "BE");

        Delivery delivery = Delivery.createNew(orderIdObj, pickupAddress, deliveryAddress);
        when(deliveryRepository.findById(orderIdObj)).thenReturn(delivery);

        // Act
        deliveryService.DeliveryReady(orderId);

        // Assert
        assertEquals(OrderStatus.READY, delivery.getOrderStatus());
        verify(deliveryRepository).findById(orderIdObj);
        verify(deliveryRepository).save(delivery);
    }

    @Test
    void deliveryReady_WithNonExistingDelivery_ShouldThrowException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderId orderIdObj = new OrderId(orderId);
        when(deliveryRepository.findById(orderIdObj)).thenThrow(new EntityNotFoundException("Delivery not found"));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> deliveryService.DeliveryReady(orderId));
        verify(deliveryRepository).findById(orderIdObj);
    }

    @Test
    void deliveryReady_WhenDeliveryAlreadyReady_ShouldThrowIllegalStateException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderId orderIdObj = new OrderId(orderId);
        Address pickupAddress = new Address("Restaurant St", "1", null, "1000", "Brussels", "BE");
        Address deliveryAddress = new Address("Customer St", "2", "A", "1000", "Brussels", "BE");

        Delivery delivery = Delivery.createNew(orderIdObj, pickupAddress, deliveryAddress);
        delivery.markAsReady();

        when(deliveryRepository.findById(orderIdObj)).thenReturn(delivery);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> deliveryService.DeliveryReady(orderId));
        verify(deliveryRepository).findById(orderIdObj);
        verify(deliveryRepository, never()).save(any(Delivery.class));
    }

    @Test
    void createDelivery_ShouldPassCorrectDeliveryToRepository() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Address pickupAddress = new Address("Restaurant St", "1", null, "1000", "Brussels", "BE");
        Address deliveryAddress = new Address("Customer St", "2", "A", "1000", "Brussels", "BE");

        ArgumentCaptor<Delivery> deliveryCaptor = ArgumentCaptor.forClass(Delivery.class);

        // Act
        Delivery result = deliveryService.createDelivery(orderId, pickupAddress, deliveryAddress);

        // Assert
        verify(deliveryRepository).save(deliveryCaptor.capture());
        Delivery capturedDelivery = deliveryCaptor.getValue();

        assertEquals(result, capturedDelivery);
        assertEquals(new OrderId(orderId), capturedDelivery.getOrderId());
        assertEquals(DeliveryStatus.OPEN, capturedDelivery.getStatus());
        assertEquals(OrderStatus.ACCEPTED, capturedDelivery.getOrderStatus());
    }
}