package be.kdg.sa.backend.infrastructure.messaging;

import be.kdg.sa.backend.application.publisher.DeliveryEventPublisher;
import be.kdg.sa.backend.config.RabbitMQTopology;
import be.kdg.sa.backend.events.DeliveryStatusChangedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RabbitMqDeliveryEventPublisher implements DeliveryEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public RabbitMqDeliveryEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    @Override
    public void publishDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQTopology.EXCHANGE_DELIVERY,
                RabbitMQTopology.DELIVERY_ORDER_STATUS_CHANGED,
                event,
                message -> {
                    message.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                    message.getMessageProperties().setCorrelationId(event.orderId().toString());
                    return message;
                }
        );
    }

}
