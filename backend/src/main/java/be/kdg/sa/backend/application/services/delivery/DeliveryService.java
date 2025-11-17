package be.kdg.sa.backend.application.services.delivery;


import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.domain.delivery.OrderId;
import be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.DeliveryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    public Delivery createDelivery(UUID orderId, Address pickupAddress, Address deliveryAddress) {
        Delivery delivery = Delivery.createNew(new OrderId(orderId), pickupAddress, deliveryAddress);
        deliveryRepository.save(delivery);
        return delivery;
    }

    public Delivery getById(OrderId orderId) {
        return deliveryRepository.findById(orderId);
    }

    public void DeliveryReady(UUID id) {
        Delivery delivery = deliveryRepository.findById(new OrderId(id));
        delivery.markAsReady();
        deliveryRepository.save(delivery);
    }
}
