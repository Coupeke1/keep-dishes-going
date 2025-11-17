package be.kdg.sa.backend.handlers;

import be.kdg.sa.backend.config.RabbitMQTopology;
import be.kdg.sa.backend.domain.order.Order;
import be.kdg.sa.backend.domain.order.OrderId;
import be.kdg.sa.backend.domain.order.OrderRepository;
import be.kdg.sa.backend.events.DeliveryStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class DeliveryStatusChangedHandler {
    private final OrderRepository orders;

    public DeliveryStatusChangedHandler(OrderRepository orders) {
        this.orders = orders;
    }

    @RabbitListener(queues = RabbitMQTopology.QUEUE_DELIVERY_ORDER_STATUS_CHANGED)
    @Transactional
    public void handleDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
        log.info("Received DeliveryStatusChangedEvent for order {} (status={})",
                event.orderId(), event.status());

        var orderOpt = orders.findById(new OrderId(event.orderId()));
        if (orderOpt.isEmpty()) {
            log.warn("Order {} not found — skipping status update", event.orderId());
            return;
        }

        Order order = orderOpt.get();
        switch (event.status()) {
            case PICKED_UP -> order.markAsOutForDelivery();
            case DELIVERED -> order.markAsDelivered();
            case REJECTED -> order.reject("Delivery cancelled");
            default -> log.info("Ignoring unhandled delivery status: {}", event.status());
        }

        orders.save(order);
        log.info("Order {} updated to reflect delivery status {}", event.orderId(), event.status());
    }
}
