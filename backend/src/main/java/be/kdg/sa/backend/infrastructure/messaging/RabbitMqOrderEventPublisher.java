package be.kdg.sa.backend.infrastructure.messaging;

import be.kdg.sa.backend.application.publisher.OrderEventPublisher;
import be.kdg.sa.backend.config.RabbitMQTopology;
import be.kdg.sa.backend.events.OrderDecisionEvent;
import be.kdg.sa.backend.events.OrderReadyPublishedEvent;
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
    public void publishOrderDecision(OrderDecisionEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQTopology.EXCHANGE_ORDER,
                "order.decision",
                event,
                msg -> {
                    msg.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                    msg.getMessageProperties().setCorrelationId(event.orderId().toString());
                    return msg;
                }
        );
    }

    @Override
    public void publishOrderReady(OrderReadyPublishedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQTopology.EXCHANGE_ORDER,
                "order.ready",
                event,
                msg -> {
                    msg.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                    msg.getMessageProperties().setCorrelationId(event.orderId().toString());
                    return msg;
                }
        );
    }
}
