package be.kdg.sa.backend.application.publisher;

import be.kdg.sa.backend.events.OrderCreatedEvent;

public interface RestaurantEventPublisher {
    void publishOrderCreated(OrderCreatedEvent createdEvent);
}
