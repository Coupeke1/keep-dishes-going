package be.kdg.sa.backend.domain.restaurant;

import java.util.List;
import java.util.Optional;

public interface RestaurantCatalog {
    List<Restaurant> getAllRestaurants();
    Optional<Restaurant> getRestaurant(RestaurantId id);
    boolean isRestaurantOpen(RestaurantId restaurantId);
    Optional<Dish> getDish(RestaurantId restaurantId, DishId dishId);
    List<Dish> getAllDishes(RestaurantId restaurantId);
}