package be.kdg.sa.backend.api;

import be.kdg.sa.backend.api.dto.restaurant.DishDto;
import be.kdg.sa.backend.api.dto.restaurant.RestaurantDto;
import be.kdg.sa.backend.application.RestaurantService;
import be.kdg.sa.backend.domain.restaurant.Dish;
import be.kdg.sa.backend.domain.restaurant.Restaurant;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private final RestaurantService restaurants;

    public RestaurantController(RestaurantService restaurants) {
        this.restaurants = restaurants;
    }

    @GetMapping({"/", ""})
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<RestaurantDto>> findAll() {
        List<Restaurant> restaurants = this.restaurants.findAll();
        List<RestaurantDto> restaurantDtos = restaurants.stream().map(RestaurantDto::from).toList();
        return ResponseEntity.ok(restaurantDtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<RestaurantDto> findById(@PathVariable("id") final UUID id) {
        final Restaurant restaurant = restaurants.findById(new RestaurantId(id));
        RestaurantDto restaurantDto = RestaurantDto.from(restaurant);
        return ResponseEntity.ok(restaurantDto);
    }

    @GetMapping("/{id}/dishes")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<DishDto>> findDishesByRestaurantId(@PathVariable("id") final UUID id) {
        List<Dish> dishes = restaurants.findAllDishes(new RestaurantId(id));
        List<DishDto> dishDtos = dishes.stream().map(DishDto::from).toList();
        return ResponseEntity.ok(dishDtos);
    }
}
