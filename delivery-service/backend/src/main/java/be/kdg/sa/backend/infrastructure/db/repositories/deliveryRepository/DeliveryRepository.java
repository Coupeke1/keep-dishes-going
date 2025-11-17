package be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository;


import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.domain.delivery.DeliveryStatus;
import be.kdg.sa.backend.domain.delivery.OrderId;
import be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.jpa.JpaDeliveryEntity;

import java.util.List;
import java.util.UUID;

public interface DeliveryRepository {
    Delivery findById(OrderId orderId);

    void save(Delivery delivery);

    List<JpaDeliveryEntity> findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(UUID driverUuid, DeliveryStatus deliveryStatus);
}
