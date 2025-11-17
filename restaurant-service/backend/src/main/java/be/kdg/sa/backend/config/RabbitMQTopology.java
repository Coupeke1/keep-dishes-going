package be.kdg.sa.backend.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQTopology {
    public static final String EXCHANGE_RESTAURANT = "exchange.restaurant";
    public static final String EXCHANGE_DELIVERY = "exchange.delivery";
    public static final String EXCHANGE_ORDER = "exchange.order";

    public static final String QUEUE_ORDER_DECISION = "queue.order.decision";
    public static final String QUEUE_ORDER_READY = "queue.order.ready";
    public static final String QUEUE_RESTAURANT = "queue.restaurant";

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
    Queue restaurantQueue() {
        return QueueBuilder.durable(QUEUE_RESTAURANT).build();
    }

    @Bean
    Queue restaurantOrderDecisionQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_DECISION).build();
    }

    @Bean
    Queue restaurantOrderReadyQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_READY).build();
    }

    @Bean
    Binding restaurantQueueBinding() {
        return BindingBuilder.bind(restaurantQueue())
                .to(restaurantExchange())
                .with("restaurant.*");
    }

    @Bean
    Binding orderDecisionBinding() {
        return BindingBuilder.bind(restaurantOrderDecisionQueue())
                .to(orderExchange())
                .with("order.decision");
    }

    @Bean
    Binding orderReadyBinding() {
        return BindingBuilder.bind(restaurantOrderReadyQueue())
                .to(orderExchange())
                .with("order.ready");
    }
}
