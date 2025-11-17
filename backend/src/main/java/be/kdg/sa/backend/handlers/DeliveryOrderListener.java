package be.kdg.sa.backend.handlers;

import be.kdg.sa.backend.application.services.delivery.DeliveryService;
import be.kdg.sa.backend.config.RabbitMQTopology;
import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.events.DeliveryOrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeliveryOrderListener {

    private final DeliveryService deliveryService;

    public DeliveryOrderListener(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RabbitListener(queues = RabbitMQTopology.QUEUE_DELIVERY_NEW_DELIVERY)
    public void handleNewDelivery(DeliveryOrderEvent event) {
        log.info("Received DeliveryOrderEvent for orderId={}", event.orderId());

        String restaurantBus = event.restaurantAddress().busNumber();
        log.info("restaurant busNumber='{}' (len={})", restaurantBus, restaurantBus != null ? restaurantBus.length() : 0);

        String customerBus = event.customer().address().busNumber();
        log.info("customer  busNumber='{}' (len={})", customerBus, customerBus != null ? customerBus.length() : 0);

        Address pickupAddress = new Address(
                event.restaurantAddress().street(),
                event.restaurantAddress().houseNumber(),
                restaurantBus,
                event.restaurantAddress().postalCode(),
                event.restaurantAddress().city(),
                event.restaurantAddress().country()
        );

        Address delivery = new Address(
                event.customer().address().street(),
                event.customer().address().houseNumber(),
                customerBus,
                event.customer().address().postalCode(),
                event.customer().address().city(),
                event.customer().address().country()
        );

        Delivery created = deliveryService.createDelivery(event.orderId(), pickupAddress, delivery);
        log.info("Created new delivery {} for order {}", created.getOrderId().id(), event.orderId());
    }
}

