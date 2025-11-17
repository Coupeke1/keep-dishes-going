package be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository;

import be.kdg.sa.backend.domain.owner.OwnerId;
import be.kdg.sa.backend.domain.restaurant.Restaurant;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import be.kdg.sa.backend.domain.restaurant.dish.Dish;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository {
    Optional<Restaurant> findById(RestaurantId id);

    Optional<Restaurant> findByOwnerId(UUID ownerId);

    void save(Restaurant restaurant);

    Collection<Restaurant> findAll();

    Collection<Dish> findAllDishesByRestaurantId(RestaurantId id);

    boolean existsByOwnerId(OwnerId ownerId);
}
