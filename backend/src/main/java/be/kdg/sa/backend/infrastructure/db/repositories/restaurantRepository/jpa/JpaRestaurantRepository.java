package be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository.jpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaRestaurantRepository extends JpaRepository<JpaRestaurantEntity, UUID> {

    @EntityGraph(attributePaths = {"address", "menu", "menu.dishes"})
    Optional<JpaRestaurantEntity> findById(UUID uuid);

    boolean existsByOwnerId(UUID ownerId);

    @EntityGraph(attributePaths = {"address", "menu", "menu.dishes"})
    Optional<JpaRestaurantEntity> findByOwnerId(UUID ownerId);
}
