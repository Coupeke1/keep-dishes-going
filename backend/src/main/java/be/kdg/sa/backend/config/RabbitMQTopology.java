package be.kdg.sa.backend.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQTopology {
    public static final String EXCHANGE_DELIVERY = "exchange.delivery";
    public static final String QUEUE_DELIVERY_NEW_DELIVERY = "queue.delivery.new-order";
    public static final String QUEUE_DELIVERY_ORDER_READY = "queue.delivery.order-ready";

    public static final String DELIVERY_ORDER_STATUS_CHANGED = "delivery.order-status-changed";


    @Bean
    TopicExchange deliveryExchange() {
        return new TopicExchange(EXCHANGE_DELIVERY, true, false);
    }

    @Bean
    Queue deliveryNewDeliveryQueue() {
        return QueueBuilder.durable(QUEUE_DELIVERY_NEW_DELIVERY).build();
    }

    @Bean
    Binding deliveryNewDeliveryBinding() {
        return BindingBuilder.bind(deliveryNewDeliveryQueue())
                .to(deliveryExchange())
                .with("delivery.new-order");
    }

    @Bean
    Queue deliveryOrderReadyQueue() {
        return QueueBuilder.durable(QUEUE_DELIVERY_ORDER_READY).build();
    }

    @Bean
    Binding deliveryOrderReadyBinding() {
        return BindingBuilder.bind(deliveryOrderReadyQueue())
                .to(deliveryExchange())
                .with("delivery.order-ready");
    }
}

