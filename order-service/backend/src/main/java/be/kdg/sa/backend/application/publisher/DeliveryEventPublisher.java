package be.kdg.sa.backend.application.publisher;

import be.kdg.sa.backend.events.DeliveryOrderEvent;
import be.kdg.sa.backend.events.OrderReadyForDeliveryEvent;

public interface DeliveryEventPublisher {
    void publishDeliveryOrder(DeliveryOrderEvent deliveryEvent);
    void publishDeliveryOrderReady(OrderReadyForDeliveryEvent OrderReadyEvent);

}
