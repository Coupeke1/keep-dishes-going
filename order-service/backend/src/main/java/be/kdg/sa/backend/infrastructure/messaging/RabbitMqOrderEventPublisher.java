package be.kdg.sa.backend.infrastructure.messaging;

import be.kdg.sa.backend.application.publisher.OrderEventPublisher;
import be.kdg.sa.backend.config.RabbitMQTopology;
import be.kdg.sa.backend.events.OrderTimeoutEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RabbitMqOrderEventPublisher implements OrderEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public RabbitMqOrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishOrderTimeout(OrderTimeoutEvent timeoutEvent) {
        rabbitTemplate.convertAndSend(RabbitMQTopology.EXCHANGE_ORDER, "order.timeout", timeoutEvent, message -> {
            message.getMessageProperties().setMessageId(UUID.randomUUID().toString());
            message.getMessageProperties().setCorrelationId(timeoutEvent.orderId().toString());
            return message;
        });
    }
}
