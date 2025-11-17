package be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaRestaurantOrderRepository extends JpaRepository<JpaRestaurantOrderEntity, UUID> {
    List<JpaRestaurantOrderEntity> findByRestaurantIdAndStatus(UUID restaurantId, String status);
}
