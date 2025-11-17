package be.kdg.sa.backend.infrastructure.db.repositories.deliveryRepository.jpa;

import be.kdg.sa.backend.domain.delivery.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaDeliveryRepository extends JpaRepository<JpaDeliveryEntity, UUID> {
    List<JpaDeliveryEntity> findByAssignedDriverIdAndStatusOrderByDeliveryTimeAsc(UUID assignedDriverId, DeliveryStatus status);


}
