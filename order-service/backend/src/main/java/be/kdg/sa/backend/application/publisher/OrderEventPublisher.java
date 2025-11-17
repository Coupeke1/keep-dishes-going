package be.kdg.sa.backend.application.publisher;

import be.kdg.sa.backend.events.OrderTimeoutEvent;

public interface OrderEventPublisher {
    void publishOrderTimeout(OrderTimeoutEvent timeoutEvent);
}
