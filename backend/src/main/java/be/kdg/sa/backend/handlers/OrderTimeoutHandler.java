package be.kdg.sa.backend.handlers;

import be.kdg.sa.backend.config.RabbitMQTopology;
import be.kdg.sa.backend.domain.order.Order;
import be.kdg.sa.backend.domain.order.OrderId;
import be.kdg.sa.backend.domain.order.OrderRepository;
import be.kdg.sa.backend.domain.order.OrderStatus;
import be.kdg.sa.backend.events.OrderTimeoutEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderTimeoutHandler {
    private final OrderRepository orders;

    public OrderTimeoutHandler(OrderRepository orders) {
        this.orders = orders;
    }

    @RabbitListener(queues = RabbitMQTopology.QUEUE_ORDER_TIMEOUT_DLQ)
    @Transactional
    void handleTimeout(OrderTimeoutEvent event) {
        Order order = orders.findById(new OrderId(event.orderId())).orElse(null);
        if (order == null) return;

        if (order.getStatus() == OrderStatus.PLACED) {
            order.reject("No response from restaurant (timeout)");
            orders.save(order);
            log.warn("Order {} automatically rejected after timeout", event.orderId());
        } else {
            log.info("Ignoring timeout for order {} (already decided: {})", event.orderId(), order.getStatus());
        }
    }
}
