package be.kdg.sa.backend.application;

import be.kdg.sa.backend.domain.restaurant.Dish;
import be.kdg.sa.backend.domain.restaurant.Restaurant;
import be.kdg.sa.backend.domain.restaurant.RestaurantCatalog;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class RestaurantService {

    private final RestaurantCatalog restaurants;

    public RestaurantService(final RestaurantCatalog restaurants) {
        this.restaurants = restaurants;
    }

    public List<Restaurant> findAll() {
        return restaurants.getAllRestaurants();
    }
    public Restaurant findById(RestaurantId id) {
        return restaurants.getRestaurant(id).orElseThrow(id::notFound);
    }

    public List<Dish> findAllDishes(RestaurantId restaurantId) {
        return restaurants.getAllDishes(restaurantId);
    }
}
