package be.kdg.sa.backend.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQTopology {
    public static final String EXCHANGE_ORDER = "exchange.order";
    public static final String EXCHANGE_RESTAURANT = "exchange.restaurant";
    public static final String EXCHANGE_DELIVERY = "exchange.delivery";

    public static final String QUEUE_ORDER_DECISION = "queue.order.decision";
    public static final String QUEUE_ORDER_READY = "queue.order.ready";
    public static final String QUEUE_ORDER_TIMEOUT = "queue.order.timeout";
    public static final String QUEUE_ORDER_TIMEOUT_DLQ = "queue.order.timeout.dlx";
    public static final String QUEUE_DELIVERY_NEW_DELIVERY = "queue.delivery.new-order";
    public static final String QUEUE_DELIVERY_ORDER_STATUS_CHANGED = "queue.delivery.order-status-changed";

    public static final int QUEUE_TIMEOUT = 300_000;

    @Bean
    TopicExchange orderExchange() {
        return new TopicExchange(EXCHANGE_ORDER, true, false);
    }

    @Bean
    TopicExchange restaurantExchange() {
        return new TopicExchange(EXCHANGE_RESTAURANT, true, false);
    }

    @Bean
    TopicExchange deliveryExchange() {
        return new TopicExchange(EXCHANGE_DELIVERY, true, false);
    }

    @Bean
    Queue orderDecisionQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_DECISION).build();
    }

    @Bean
    Queue orderReadyQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_READY).build();
    }

    @Bean
    Queue orderTimeoutQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_TIMEOUT)
                .withArgument("x-dead-letter-exchange", EXCHANGE_ORDER)
                .withArgument("x-dead-letter-routing-key", "order.timeout.dlx")
                .withArgument("x-message-ttl", QUEUE_TIMEOUT)
                .build();
    }

    @Bean
    Queue orderTimeoutDLQ() {
        return QueueBuilder.durable(QUEUE_ORDER_TIMEOUT_DLQ).build();
    }

    @Bean
    Queue deliveryNewDeliveryQueue() {
        return QueueBuilder.durable(QUEUE_DELIVERY_NEW_DELIVERY).build();
    }

    @Bean
    Binding decisionBinding() {
        return BindingBuilder.bind(orderDecisionQueue())
                .to(orderExchange()).with("order.decision");
    }

    @Bean
    Binding readyBinding() {
        return BindingBuilder.bind(orderReadyQueue())
                .to(orderExchange()).with("order.ready");
    }

    @Bean
    Binding timeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue())
                .to(orderExchange()).with("order.timeout");
    }

    @Bean
    Binding timeoutDLXBinding() {
        return BindingBuilder.bind(orderTimeoutDLQ())
                .to(orderExchange()).with("order.timeout.dlx");
    }

    @Bean
    Binding newDeliveryBinding() {
        return BindingBuilder.bind(deliveryNewDeliveryQueue())
                .to(deliveryExchange()).with("delivery.new-order");
    }

    @Bean
    Queue deliveryOrderStatusChangedQueue() {
        return QueueBuilder.durable(QUEUE_DELIVERY_ORDER_STATUS_CHANGED).build();
    }

    @Bean
    Binding deliveryOrderStatusChangedBinding() {
        return BindingBuilder.bind(deliveryOrderStatusChangedQueue())
                .to(deliveryExchange())
                .with("delivery.order-status-changed");
    }
}
