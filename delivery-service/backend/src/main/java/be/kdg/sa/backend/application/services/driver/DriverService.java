package be.kdg.sa.backend.application.services.driver;

import be.kdg.sa.backend.api.delivery.dto.CompletedDeliveriesResponse;
import be.kdg.sa.backend.api.delivery.dto.CompletedDeliveryItemDto;
import be.kdg.sa.backend.application.events.DeliveryStatusChangedDomainEvent;
import be.kdg.sa.backend.application.services.payment.PayoutService;
import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.domain.delivery.DeliveryStatus;
import be.kdg.sa.backend.domain.delivery.OrderId;
import be.kdg.sa.backend.domain.driver.Driver;
import be.kdg.sa.backend.domain.driver.DriverId;
import be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.DeliveryRepository;
import be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.jpa.JpaDeliveryEntity;
import be.kdg.sa.backend.infrastructure.db.repositories.driverRepository.DriverRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DriverService {

    private final DriverRepository driverRepository;
    private final DeliveryRepository deliveryRepository;
    private final PayoutService payoutService;
    private final ApplicationEventPublisher publisher;

    public DriverService(DriverRepository driverRepository,
                         DeliveryRepository deliveryRepository,
                         PayoutService payoutService,
                         ApplicationEventPublisher publisher) {
        this.driverRepository = driverRepository;
        this.deliveryRepository = deliveryRepository;
        this.payoutService = payoutService;
        this.publisher = publisher;
    }

    public Driver registerDriver(DriverId driverId, String name, String email, String phoneNumber,
                                 String accountNumber, Address address) {
        Driver driver = new Driver(driverId, name, email, phoneNumber, accountNumber, address, null);
        driverRepository.save(driver);
        return driver;
    }

    public Driver getById(DriverId driverId) {
        return driverRepository.findById(driverId);
    }

    public void claimDelivery(UUID driverUuid, UUID deliveryUuid) {
        DriverId driverId = new DriverId(driverUuid);
        Driver driver = driverRepository.findById(driverId);
        driver.ensureCanClaimDelivery();

        Delivery delivery = deliveryRepository.findById(new OrderId(deliveryUuid));
        delivery.claim(driverId);
        driver.markAsAssigned(new OrderId(deliveryUuid));

        driverRepository.save(driver);
        deliveryRepository.save(delivery);
    }

    public void startDelivery(UUID driverUuid) {
        Driver driver = driverRepository.findById(new DriverId(driverUuid));
        driver.ensureCanCompleteDelivery();

        Delivery delivery = deliveryRepository.findById(driver.getActiveDeliveryId());
        delivery.start();

        publisher.publishEvent(new DeliveryStatusChangedDomainEvent(
                delivery.getOrderId().id(),
                delivery.getOrderStatus()
        ));

        deliveryRepository.save(delivery);
    }

    public void cancelDelivery(UUID driverUuid) {
        Driver driver = driverRepository.findById(new DriverId(driverUuid));
        driver.ensureCanCompleteDelivery();

        OrderId deliveryId = driver.getActiveDeliveryId();
        Delivery delivery = deliveryRepository.findById(deliveryId);

        delivery.cancel();
        driver.markDeliveryCancelled();

        deliveryRepository.save(delivery);
        driverRepository.save(driver);
    }

    public BigDecimal completeDelivery(UUID driverUuid) {
        Driver driver = driverRepository.findById(new DriverId(driverUuid));
        driver.ensureCanCompleteDelivery();

        OrderId deliveryId = driver.getActiveDeliveryId();
        Delivery delivery = deliveryRepository.findById(deliveryId);

        if (delivery.getPickupTime() == null) {
            throw new IllegalStateException("Delivery heeft geen pickupTime");
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal payout = payoutService.calculatePayout(delivery.getPickupTime(), now);

        delivery.complete(payout);
        driver.completeDelivery();

        deliveryRepository.save(delivery);
        driverRepository.save(driver);

        publisher.publishEvent(new DeliveryStatusChangedDomainEvent(
                delivery.getOrderId().id(),
                delivery.getOrderStatus()
        ));

        return payout;
    }

    public CompletedDeliveriesResponse getCompletedDeliveriesForDriver(DriverId driverId) {
        UUID driverUuid = driverId.id();
        List<JpaDeliveryEntity> entities = deliveryRepository
                .findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(driverUuid, DeliveryStatus.DELIVERED);

        List<CompletedDeliveryItemDto> items = new ArrayList<>();
        BigDecimal runningTotal = BigDecimal.ZERO;

        for (JpaDeliveryEntity e : entities) {
            BigDecimal payout = e.getPrice() != null ? e.getPrice() : BigDecimal.ZERO;
            payout = payout.setScale(2, RoundingMode.CEILING);
            runningTotal = runningTotal.add(payout).setScale(2, RoundingMode.CEILING);

            items.add(new CompletedDeliveryItemDto(
                    e.getOrderId(),
                    e.getPickupTime(),
                    e.getDeliveryTime(),
                    payout,
                    runningTotal
            ));
        }

        return new CompletedDeliveriesResponse(driverUuid, runningTotal, items);
    }
}
