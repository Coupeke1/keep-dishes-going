package be.kdg.sa.backend.application.publisher;

import be.kdg.sa.backend.application.events.DeliveryStatusChangedDomainEvent;
import be.kdg.sa.backend.events.DeliveryStatusChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class DeliveryIntegrationPublisher {

    private final DeliveryEventPublisher deliveryPublisher;

    public DeliveryIntegrationPublisher(DeliveryEventPublisher deliveryPublisher) {
        this.deliveryPublisher = deliveryPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeliveryCompleted(DeliveryStatusChangedDomainEvent evt) {
        DeliveryStatusChangedEvent event = new DeliveryStatusChangedEvent(evt.orderId(), evt.status());
        deliveryPublisher.publishDeliveryStatusChanged(event);
    }
}
