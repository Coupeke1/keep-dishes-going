package be.kdg.sa.backend.handlers;

import be.kdg.sa.backend.application.services.delivery.DeliveryService;
import be.kdg.sa.backend.config.RabbitMQTopology;
import be.kdg.sa.backend.events.OrderReadyForDeliveryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class DeliveryOrderReadyListener {

    private final DeliveryService deliveryService;

    public DeliveryOrderReadyListener(DeliveryService deliveryService) {
        this.deliveryService = Objects.requireNonNull(deliveryService, "deliveryService must not be null");
    }

    @RabbitListener(queues = RabbitMQTopology.QUEUE_DELIVERY_ORDER_READY)
    public void handleOrderReady(OrderReadyForDeliveryEvent event) {
        log.info("Order ready for delivery received: orderId={}, restaurantId={}",
                event.orderId(), event.restaurantId());

        deliveryService.DeliveryReady(event.orderId());

    }
}
