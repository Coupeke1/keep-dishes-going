package be.kdg.sa.backend.handlers;

import be.kdg.sa.backend.application.publisher.DeliveryEventPublisher;
import be.kdg.sa.backend.config.RabbitMQTopology;
import be.kdg.sa.backend.domain.order.OrderId;
import be.kdg.sa.backend.domain.order.OrderRepository;
import be.kdg.sa.backend.events.OrderReadyEvent;
import be.kdg.sa.backend.events.OrderReadyForDeliveryEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderReadyHandler {
    private final OrderRepository orders;
    private final DeliveryEventPublisher deliveryPublisher;

    public OrderReadyHandler(OrderRepository orders, DeliveryEventPublisher deliveryPublisher) {
        this.orders = orders;
        this.deliveryPublisher = deliveryPublisher;
    }

    @RabbitListener(queues = RabbitMQTopology.QUEUE_ORDER_READY)
    @Transactional
    public void handleOrderReady(OrderReadyEvent event) {
        log.info("Received OrderReadyPublishedEvent for order {}", event.orderId());

        var order = orders.findById(new OrderId(event.orderId()))
                .orElse(null);

        if (order == null) {
            log.warn("Order {} not found — skipping", event.orderId());
            return;
        }

        try {
            order.markAsReady();
            orders.save(order);
            log.info("Order {} marked as READY", event.orderId());

            var deliveryEvent = new OrderReadyForDeliveryEvent(
                    event.orderId(),
                    event.restaurantId()
            );
            deliveryPublisher.publishDeliveryOrderReady(deliveryEvent);

        } catch (Exception ex) {
            log.error("Failed to process ready event for order {}: {}", event.orderId(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
