package be.kdg.sa.backend.handlers;

import be.kdg.sa.backend.application.services.restaurant.RestaurantOrderService;
import be.kdg.sa.backend.config.RabbitMQTopology;
import be.kdg.sa.backend.events.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RestaurantMessageHandler {
    private final RestaurantOrderService restaurantOrderService;

    public RestaurantMessageHandler(RestaurantOrderService restaurantOrderService) {
        this.restaurantOrderService = restaurantOrderService;
    }

    @RabbitListener(queues = RabbitMQTopology.QUEUE_RESTAURANT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("[RestaurantService] Received new order: {} for restaurant {}", event.orderId(), event.restaurantId());
        restaurantOrderService.createOrderFromEvent(event);
    }
}
