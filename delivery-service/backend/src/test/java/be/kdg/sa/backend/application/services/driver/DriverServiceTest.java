package be.kdg.sa.backend.application.services.driver;

import be.kdg.sa.backend.api.delivery.dto.CompletedDeliveriesResponse;
import be.kdg.sa.backend.application.events.DeliveryStatusChangedDomainEvent;
import be.kdg.sa.backend.application.services.payment.PayoutService;
import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.domain.delivery.DeliveryStatus;
import be.kdg.sa.backend.domain.delivery.OrderId;
import be.kdg.sa.backend.domain.delivery.OrderStatus;
import be.kdg.sa.backend.domain.driver.Driver;
import be.kdg.sa.backend.domain.driver.DriverId;
import be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.DeliveryRepository;
import be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.jpa.JpaDeliveryEntity;
import be.kdg.sa.backend.infrastructure.db.repositories.driverRepository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private PayoutService payoutService;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private DriverService driverService;

    private Address address;

    @BeforeEach
    void setup() {
        address = new Address("Main", "1", null, "1000", "Brussels", "BE");
    }

    @Test
    void registerDriver_shouldCreateAndSaveDriver() {
        UUID keycloakId = UUID.randomUUID();
        DriverId driverId = new DriverId(keycloakId);
        String name = "John";
        String email = "john@test.com";
        String phone = "12345";
        String account = "BE123";

        Driver driver = driverService.registerDriver(driverId, name, email, phone, account, address);

        assertThat(driver.getName()).isEqualTo(name);
        assertThat(driver.getEmail()).isEqualTo(email);
        assertThat(driver.getPhoneNumber()).isEqualTo(phone);
        assertThat(driver.getAccountNumber()).isEqualTo(account);
        assertThat(driver.getAddress()).isEqualTo(address);

        verify(driverRepository).save(driver);
    }

    @Test
    void registerDriver_withNullAddress_shouldStillSave() {
        UUID keycloakId = UUID.randomUUID();
        DriverId driverId = new DriverId(keycloakId);
        Driver driver = driverService.registerDriver(driverId, "Name", "n@t.be", "000", "BE00", null);
        assertThat(driver.getAddress()).isNull();
        verify(driverRepository).save(driver);
    }

    @Test
    void getById_shouldReturnDriver() {
        DriverId id = new DriverId(UUID.randomUUID());
        Driver expected = new Driver(id, "John", "j@t", "123", "BE");
        when(driverRepository.findById(id)).thenReturn(expected);

        Driver found = driverService.getById(id);

        assertThat(found).isEqualTo(expected);
        verify(driverRepository).findById(id);
    }

    @Test
    void claimDelivery_shouldAssignDriverAndUpdateDelivery() {
        UUID driverUuid = UUID.randomUUID();
        UUID deliveryUuid = UUID.randomUUID();
        DriverId driverId = new DriverId(driverUuid);
        OrderId orderId = new OrderId(deliveryUuid);

        Driver driver = new Driver(driverId, "John", "j@t", "123", "BE");
        Delivery delivery = Delivery.createNew(orderId,
                new Address("Pickup", "1", null, "1000", "BXL", "BE"),
                new Address("Drop", "2", null, "1000", "BXL", "BE"));

        when(driverRepository.findById(driverId)).thenReturn(driver);
        when(deliveryRepository.findById(orderId)).thenReturn(delivery);

        driverService.claimDelivery(driverUuid, deliveryUuid);

        assertThat(driver.hasActiveDelivery()).isTrue();
        assertThat(delivery.getAssignedDriverId()).isEqualTo(driverId);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CLAIMED);

        verify(driverRepository).save(driver);
        verify(deliveryRepository).save(delivery);
    }

    @Test
    void claimDelivery_shouldThrow_ifDriverHasActiveDelivery() {
        UUID driverUuid = UUID.randomUUID();
        DriverId driverId = new DriverId(driverUuid);
        Driver driver = new Driver(driverId, "J", "E", "T", "BE", address, new OrderId(UUID.randomUUID()));

        when(driverRepository.findById(driverId)).thenReturn(driver);

        assertThatThrownBy(() -> driverService.claimDelivery(driverUuid, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Driver already has an active delivery");
    }

    @Test
    void claimDelivery_shouldThrow_ifDeliveryNotFound() {
        UUID driverUuid = UUID.randomUUID();
        DriverId driverId = new DriverId(driverUuid);
        Driver driver = new Driver(driverId, "J", "E", "T", "BE");

        when(driverRepository.findById(driverId)).thenReturn(driver);
        when(deliveryRepository.findById(any())).thenThrow(new IllegalArgumentException("not found"));

        assertThatThrownBy(() -> driverService.claimDelivery(driverUuid, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void startDelivery_shouldUpdateDeliveryAndPublishEvent() {
        UUID driverUuid = UUID.randomUUID();
        DriverId driverId = new DriverId(driverUuid);
        OrderId deliveryId = new OrderId(UUID.randomUUID());

        Driver driver = new Driver(driverId, "Name", "E", "T", "BE", address, deliveryId);
        Delivery delivery = Delivery.createNew(deliveryId,
                new Address("Pickup", "1", null, "1000", "C", "BE"),
                new Address("Drop", "2", null, "1000", "C", "BE"));
        delivery.claim(driverId);
        delivery.markAsReady();

        when(driverRepository.findById(driverId)).thenReturn(driver);
        when(deliveryRepository.findById(deliveryId)).thenReturn(delivery);

        driverService.startDelivery(driverUuid);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.IN_PROGRESS);
        verify(deliveryRepository).save(delivery);
        verify(publisher).publishEvent(any(DeliveryStatusChangedDomainEvent.class));
    }

    @Test
    void startDelivery_shouldThrow_ifNoActiveDelivery() {
        UUID driverUuid = UUID.randomUUID();
        DriverId driverId = new DriverId(driverUuid);
        when(driverRepository.findById(driverId)).thenReturn(new Driver(driverId, "J", "E", "T", "BE"));

        assertThatThrownBy(() -> driverService.startDelivery(driverUuid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Driver doesn't have an active delivery");
    }

    @Test
    void cancelDelivery_shouldRevertStatusAndClearDriver() {
        UUID driverUuid = UUID.randomUUID();
        DriverId driverId = new DriverId(driverUuid);
        OrderId orderId = new OrderId(UUID.randomUUID());

        Driver driver = new Driver(driverId, "J", "E", "T", "BE", address, orderId);
        Delivery delivery = Delivery.createNew(orderId, address, address);
        when(driverRepository.findById(driverId)).thenReturn(driver);
        when(deliveryRepository.findById(orderId)).thenReturn(delivery);

        driverService.cancelDelivery(driverUuid);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.OPEN);
        assertThat(driver.hasActiveDelivery()).isFalse();
        verify(deliveryRepository).save(delivery);
        verify(driverRepository).save(driver);
    }

    @Test
    void cancelDelivery_shouldThrow_ifNoActiveDelivery() {
        UUID driverUuid = UUID.randomUUID();
        DriverId driverId = new DriverId(driverUuid);
        when(driverRepository.findById(driverId)).thenReturn(new Driver(driverId, "J", "E", "T", "BE"));

        assertThatThrownBy(() -> driverService.cancelDelivery(driverUuid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Driver doesn't have an active delivery");
    }

    @Test
    void completeDelivery_shouldCalculateAndPublishEvent() {
        UUID driverUuid = UUID.randomUUID();
        DriverId driverId = new DriverId(driverUuid);
        OrderId deliveryId = new OrderId(UUID.randomUUID());

        Driver driver = new Driver(driverId, "N", "E", "T", "BE", address, deliveryId);
        Delivery delivery = Delivery.createNew(deliveryId, address, address);
        delivery.claim(driverId);
        delivery.markAsReady();
        delivery.start();

        BigDecimal payout = new BigDecimal("10.75");

        when(driverRepository.findById(driverId)).thenReturn(driver);
        when(deliveryRepository.findById(deliveryId)).thenReturn(delivery);
        when(payoutService.calculatePayout(any(), any())).thenReturn(payout);

        BigDecimal result = driverService.completeDelivery(driverUuid);

        assertThat(result).isEqualTo(payout);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        verify(deliveryRepository).save(delivery);
        verify(driverRepository).save(driver);
        verify(publisher).publishEvent(any(DeliveryStatusChangedDomainEvent.class));
    }

    @Test
    void completeDelivery_shouldThrow_ifNoActiveDelivery() {
        UUID driverUuid = UUID.randomUUID();
        DriverId driverId = new DriverId(driverUuid);
        when(driverRepository.findById(driverId)).thenReturn(new Driver(driverId, "J", "E", "T", "BE"));

        assertThatThrownBy(() -> driverService.completeDelivery(driverUuid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Driver doesn't have an active delivery");
    }

    @Test
    void completeDelivery_shouldThrow_ifNoPickupTime() {
        UUID driverUuid = UUID.randomUUID();
        DriverId driverId = new DriverId(driverUuid);
        OrderId deliveryId = new OrderId(UUID.randomUUID());
        Driver driver = new Driver(driverId, "J", "E", "T", "BE", address, deliveryId);
        Delivery delivery = Delivery.createNew(deliveryId, address, address);
        delivery.claim(driverId);
        when(driverRepository.findById(driverId)).thenReturn(driver);
        when(deliveryRepository.findById(deliveryId)).thenReturn(delivery);

        assertThatThrownBy(() -> driverService.completeDelivery(driverUuid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Delivery heeft geen pickupTime");
    }

    @Test
    void getCompletedDeliveriesForDriver_shouldReturnAllAndSumTotal() {
        DriverId driverId = new DriverId(UUID.randomUUID());
        UUID driverUuid = driverId.id();

        LocalDateTime pickup1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime drop1 = LocalDateTime.of(2024, 1, 1, 10, 30);
        LocalDateTime pickup2 = LocalDateTime.of(2024, 1, 2, 12, 0);
        LocalDateTime drop2 = LocalDateTime.of(2024, 1, 2, 12, 45);

        JpaDeliveryEntity d1 = createJpaDeliveryEntity(UUID.randomUUID(), pickup1, drop1, new BigDecimal("12.50"));
        JpaDeliveryEntity d2 = createJpaDeliveryEntity(UUID.randomUUID(), pickup2, drop2, new BigDecimal("18.75"));

        when(deliveryRepository.findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(driverUuid, DeliveryStatus.DELIVERED))
                .thenReturn(List.of(d1, d2));

        CompletedDeliveriesResponse response = driverService.getCompletedDeliveriesForDriver(driverId);

        assertThat(response.getDriverId()).isEqualTo(driverUuid);
        assertThat(response.getTotal()).isEqualByComparingTo("31.25");
        assertThat(response.getDeliveries()).hasSize(2);
        assertThat(response.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void getCompletedDeliveriesForDriver_shouldHandleNullPrices() {
        DriverId driverId = new DriverId(UUID.randomUUID());
        UUID uuid = driverId.id();

        LocalDateTime pickup = LocalDateTime.of(2024, 1, 1, 8, 0);
        LocalDateTime drop = LocalDateTime.of(2024, 1, 1, 8, 15);
        JpaDeliveryEntity d = createJpaDeliveryEntity(UUID.randomUUID(), pickup, drop, null);

        when(deliveryRepository.findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(uuid, DeliveryStatus.DELIVERED))
                .thenReturn(List.of(d));

        CompletedDeliveriesResponse response = driverService.getCompletedDeliveriesForDriver(driverId);
        assertThat(response.getTotal()).isEqualByComparingTo("0.00");
        assertThat(response.getDeliveries()).hasSize(1);
    }

    @Test
    void getCompletedDeliveriesForDriver_shouldReturnEmptyResponseWhenNoDeliveries() {
        DriverId id = new DriverId(UUID.randomUUID());
        when(deliveryRepository.findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(id.id(), DeliveryStatus.DELIVERED))
                .thenReturn(List.of());

        CompletedDeliveriesResponse response = driverService.getCompletedDeliveriesForDriver(id);
        assertThat(response.getDeliveries()).isEmpty();
        assertThat(response.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private JpaDeliveryEntity createJpaDeliveryEntity(UUID orderId, LocalDateTime pickupTime, LocalDateTime deliveryTime, BigDecimal price) {
        return new JpaDeliveryEntity(
                orderId,
                DeliveryStatus.DELIVERED,
                OrderStatus.ACCEPTED,
                null,
                null,
                pickupTime,
                deliveryTime,
                price,
                UUID.randomUUID()
        );
    }
}
