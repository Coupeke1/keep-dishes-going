package be.kdg.sa.backend.application.publisher;

import be.kdg.sa.backend.events.OrderDecisionEvent;
import be.kdg.sa.backend.events.OrderReadyPublishedEvent;

public interface OrderEventPublisher {
    void publishOrderDecision(OrderDecisionEvent event);

    void publishOrderReady(OrderReadyPublishedEvent event);

}