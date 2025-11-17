package be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository;

import be.kdg.sa.backend.domain.restaurantOrder.RestaurantOrder;
import be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository.jpa.JpaRestaurantOrderEntity;
import be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository.jpa.JpaRestaurantOrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RestaurantOrderDb implements RestaurantOrderRepository {
    private final JpaRestaurantOrderRepository repo;

    public RestaurantOrderDb(JpaRestaurantOrderRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<RestaurantOrder> findByRestaurantIdAndStatus(UUID restaurantId, String status) {
        return repo.findByRestaurantIdAndStatus(restaurantId, status).stream()
                .map(JpaRestaurantOrderEntity::toDomain).toList();
    }

    @Override
    public Optional<RestaurantOrder> findById(UUID orderId) {
        return repo.findById(orderId).map(JpaRestaurantOrderEntity::toDomain);
    }

    @Override
    public RestaurantOrder save(RestaurantOrder entity) {
        JpaRestaurantOrderEntity restaurantOrderEntity = JpaRestaurantOrderEntity.fromDomain(entity);
        return repo.save(restaurantOrderEntity).toDomain();
    }

    @Override
    public boolean existsById(UUID uuid) {
        return repo.existsById(uuid);
    }

}
