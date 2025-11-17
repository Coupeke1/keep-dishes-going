package be.kdg.sa.backend.infrastructure.messaging;

import be.kdg.sa.backend.application.publisher.RestaurantEventPublisher;
import be.kdg.sa.backend.config.RabbitMQTopology;
import be.kdg.sa.backend.events.OrderCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RabbitMqRestaurantEventPublisher implements RestaurantEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public RabbitMqRestaurantEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishOrderCreated(OrderCreatedEvent createdEvent) {
        rabbitTemplate.convertAndSend(RabbitMQTopology.EXCHANGE_RESTAURANT, "restaurant.new-order", createdEvent, message -> {
            message.getMessageProperties().setMessageId(UUID.randomUUID().toString());
            message.getMessageProperties().setCorrelationId(createdEvent.orderId().toString());
            return message;
        });
    }
}
