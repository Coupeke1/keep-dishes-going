package be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository;

import be.kdg.sa.backend.domain.restaurantOrder.RestaurantOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantOrderRepository {
    List<RestaurantOrder> findByRestaurantIdAndStatus(UUID restaurantId, String status);

    Optional<RestaurantOrder> findById(UUID orderId);

    RestaurantOrder save(RestaurantOrder entity);

    boolean existsById(UUID uuid);
}
