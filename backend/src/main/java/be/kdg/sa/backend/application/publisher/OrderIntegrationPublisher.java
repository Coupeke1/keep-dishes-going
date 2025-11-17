package be.kdg.sa.backend.application.publisher;

import be.kdg.sa.backend.application.events.OrderPlacedDomainEvent;
import be.kdg.sa.backend.domain.order.Order;
import be.kdg.sa.backend.domain.order.OrderId;
import be.kdg.sa.backend.domain.order.OrderRepository;
import be.kdg.sa.backend.events.OrderCreatedEvent;
import be.kdg.sa.backend.events.OrderTimeoutEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderIntegrationPublisher {

    private final OrderRepository orders;
    private final RestaurantEventPublisher restaurantPublisher;
    private final OrderEventPublisher orderPublisher;

    public OrderIntegrationPublisher(OrderRepository orders,
                                     RestaurantEventPublisher restaurantPublisher,
                                     OrderEventPublisher orderPublisher) {
        this.orders = orders;
        this.restaurantPublisher = restaurantPublisher;
        this.orderPublisher = orderPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedDomainEvent evt) {
        Order order = orders.findById(new OrderId(evt.orderId()))
                .orElseThrow(() -> new IllegalStateException("Order not found for integration publish: " + evt.orderId()));

        OrderCreatedEvent created = buildOrderCreatedEvent(order);

        OrderTimeoutEvent timeout = new OrderTimeoutEvent(evt.orderId());

        restaurantPublisher.publishOrderCreated(created);
        orderPublisher.publishOrderTimeout(timeout);
    }

    private OrderCreatedEvent buildOrderCreatedEvent(Order order) {
        OrderCreatedEvent.Customer customer = getCustomer(order);

        var lines = order.getLines().stream()
                .map(l -> new OrderCreatedEvent.OrderLine(
                        l.getDishId().value(),
                        l.getDishName(),
                        l.getQuantity(),
                        l.getUnitPrice(),
                        l.getTotalPrice()
                ))
                .toList();

        String timePlaced = order.getTimePlaced() != null ? order.getTimePlaced().toString() : null;

        return new OrderCreatedEvent(order.getId().value(),
                order.getRestaurantId() != null ? order.getRestaurantId().value() : null,
                customer,
                lines,
                timePlaced,
                order.getTotalPrice());
    }

    private static OrderCreatedEvent.Customer getCustomer(Order order) {
        OrderCreatedEvent.Address address = null;
        if (order.getDeliveryAddress() != null) {
            address = new OrderCreatedEvent.Address(
                    order.getDeliveryAddress().street(),
                    order.getDeliveryAddress().houseNumber(),
                    order.getDeliveryAddress().country(),
                    order.getDeliveryAddress().city(),
                    order.getDeliveryAddress().postalCode()
            );
        }

        return new OrderCreatedEvent.Customer(
                order.getCustomerName(),
                order.getCustomerEmail(),
                address
        );
    }
}
