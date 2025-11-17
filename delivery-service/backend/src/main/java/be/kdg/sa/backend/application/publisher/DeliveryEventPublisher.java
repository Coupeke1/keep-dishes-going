package be.kdg.sa.backend.application.publisher;

import be.kdg.sa.backend.events.DeliveryStatusChangedEvent;

public interface DeliveryEventPublisher {
    void publishDeliveryStatusChanged(DeliveryStatusChangedEvent event);

}
