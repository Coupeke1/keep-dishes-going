package be.kdg.sa.backend.handlers;

import be.kdg.sa.backend.application.publisher.DeliveryEventPublisher;
import be.kdg.sa.backend.config.RabbitMQTopology;
import be.kdg.sa.backend.domain.InvalidOrderStateException;
import be.kdg.sa.backend.domain.order.Order;
import be.kdg.sa.backend.domain.order.OrderId;
import be.kdg.sa.backend.domain.order.OrderRepository;
import be.kdg.sa.backend.events.DeliveryOrderEvent;
import be.kdg.sa.backend.events.OrderDecisionEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrderDecisionHandler {
    private final OrderRepository orders;
    private final DeliveryEventPublisher deliveryPublisher;

    public OrderDecisionHandler(OrderRepository orders, DeliveryEventPublisher deliveryPublisher) {
        this.orders = orders;
        this.deliveryPublisher = deliveryPublisher;
    }

    private static DeliveryOrderEvent.Customer getCustomer(Order order) {
        DeliveryOrderEvent.Address address = null;
        if (order.getDeliveryAddress() != null) {
            address = new DeliveryOrderEvent.Address(
                    order.getDeliveryAddress().street(),
                    order.getDeliveryAddress().houseNumber(),
                    order.getDeliveryAddress().busNumber(),
                    order.getDeliveryAddress().country(),
                    order.getDeliveryAddress().city(),
                    order.getDeliveryAddress().postalCode()
            );
        }

        return new DeliveryOrderEvent.Customer(
                order.getCustomerName(),
                order.getCustomerEmail(),
                address
        );
    }

    @RabbitListener(queues = RabbitMQTopology.QUEUE_ORDER_DECISION)
    @Transactional
    public void handleOrderDecision(OrderDecisionEvent event) {
        try {
            if (!"ACCEPTED".equalsIgnoreCase(event.decision())) {
                log.info("Ignoring non-accepted decision for order {}", event.orderId());
                return;
            }

            Order order = orders.findById(new OrderId(event.orderId()))
                    .orElseThrow(() -> new InvalidOrderStateException("Order not found: " + event.orderId()));

            order.accept(event.reason());

            DeliveryOrderEvent delivery = buildDeliveryOrderEvent(order,
                    new DeliveryOrderEvent.Address(
                            event.restaurantAddress().street(),
                            event.restaurantAddress().houseNumber(),
                            event.restaurantAddress().busNumber(),
                            event.restaurantAddress().country(),
                            event.restaurantAddress().city(),
                            event.restaurantAddress().postalCode()
                    )
            );

            deliveryPublisher.publishDeliveryOrder(delivery);
            orders.save(order);

            log.info("Accepted order {} successfully", event.orderId());

        } catch (InvalidOrderStateException ex) {
            log.warn("Skipping decision for order {}: {}", event.orderId(), ex.getMessage());
        } catch (Exception ex) {
            log.error("Infrastructure failure for order {}: {}", event.orderId(), ex.getMessage(), ex);
            throw ex;
        }
    }

    private DeliveryOrderEvent buildDeliveryOrderEvent(Order order, DeliveryOrderEvent.Address address) {
        DeliveryOrderEvent.Customer customer = getCustomer(order);

        List<DeliveryOrderEvent.OrderLine> lines = order.getLines().stream()
                .map(l -> new DeliveryOrderEvent.OrderLine(
                        l.getDishId().value(),
                        l.getDishName(),
                        l.getQuantity(),
                        l.getUnitPrice(),
                        l.getTotalPrice()
                ))
                .toList();

        String timePlaced = order.getTimePlaced() != null ? order.getTimePlaced().toString() : null;

        return new DeliveryOrderEvent(order.getId().value(),
                order.getRestaurantId() != null ? order.getRestaurantId().value() : null,
                customer, address,
                lines,
                timePlaced,
                order.getTotalPrice());

    }

}
