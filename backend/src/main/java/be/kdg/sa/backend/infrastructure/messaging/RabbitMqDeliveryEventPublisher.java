package be.kdg.sa.backend.infrastructure.messaging;

import be.kdg.sa.backend.application.publisher.DeliveryEventPublisher;
import be.kdg.sa.backend.config.RabbitMQTopology;
import be.kdg.sa.backend.events.DeliveryOrderEvent;
import be.kdg.sa.backend.events.OrderReadyForDeliveryEvent;
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
    public void publishDeliveryOrder(DeliveryOrderEvent deliveryEvent) {
        rabbitTemplate.convertAndSend(RabbitMQTopology.EXCHANGE_DELIVERY, "delivery.new-order", deliveryEvent, message -> {
            message.getMessageProperties().setMessageId(UUID.randomUUID().toString());
            message.getMessageProperties().setCorrelationId(deliveryEvent.orderId().toString());
            return message;
        });
    }


    @Override
    public void publishDeliveryOrderReady(OrderReadyForDeliveryEvent readyEvent) {
        rabbitTemplate.convertAndSend(
                RabbitMQTopology.EXCHANGE_DELIVERY,
                "delivery.order-ready",
                readyEvent,
                msg -> {
                    msg.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                    msg.getMessageProperties().setCorrelationId(readyEvent.orderId().toString());
                    return msg;
                }
        );
    }
}
