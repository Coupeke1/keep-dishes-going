package be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository;

import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.domain.delivery.DeliveryStatus;
import be.kdg.sa.backend.domain.delivery.OrderId;
import be.kdg.sa.backend.infrastructure.db.repositories.EntityNotFoundException;
import be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.jpa.JpaDeliveryEntity;
import be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.jpa.JpaDeliveryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class DbDelivery implements DeliveryRepository {
    private final JpaDeliveryRepository jpaDeliveryRepository;

    public DbDelivery(JpaDeliveryRepository jpaDeliveryRepository) {
        this.jpaDeliveryRepository = Objects.requireNonNull(jpaDeliveryRepository, "jpaDeliveryRepository must not be null");
    }

    @Override
    public Delivery findById(OrderId orderId) {
        return jpaDeliveryRepository.findById(orderId.id())
                .map(JpaDeliveryEntity::toDomain)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found: " + orderId.id()));
    }

    @Override
    public void save(Delivery delivery) {
        if (delivery == null) throw new IllegalArgumentException("Delivery must not be null.");
        JpaDeliveryEntity deliveryEntity = JpaDeliveryEntity.fromDomain(delivery);
        jpaDeliveryRepository.save(deliveryEntity);
    }

    @Override
    public List<JpaDeliveryEntity> findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(UUID driverUuid, DeliveryStatus deliveryStatus) {
        return jpaDeliveryRepository.findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(driverUuid, deliveryStatus);
    }
}