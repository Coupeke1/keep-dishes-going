package be.kdg.sa.backend.api;

import be.kdg.sa.backend.api.dto.dish.CreateDishDto;
import be.kdg.sa.backend.api.dto.dish.DishDto;
import be.kdg.sa.backend.api.dto.dish.DishForOrderingDto;
import be.kdg.sa.backend.api.dto.dish.UpdateDishDto;
import be.kdg.sa.backend.api.dto.openingHours.CreateOpeningHoursDto;
import be.kdg.sa.backend.api.dto.restaurant.*;
import be.kdg.sa.backend.application.services.restaurant.RestaurantOrderService;
import be.kdg.sa.backend.application.services.restaurant.RestaurantService;
import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.restaurant.Restaurant;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import be.kdg.sa.backend.domain.restaurant.dish.Dish;
import be.kdg.sa.backend.domain.restaurant.dish.DishId;
import be.kdg.sa.backend.domain.restaurant.dish.DishPendingUpdate;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.UUID;

@CrossOrigin(originPatterns = "http://localhost:[*]")
@RestController
@RequestMapping("api/restaurants")
@Slf4j
public class RestaurantRestController {

    private final RestaurantService restaurantService;
    private final RestaurantOrderService restaurantOrderService;

    public RestaurantRestController(RestaurantService restaurantService, RestaurantOrderService restaurantOrderService) {
        this.restaurantService = restaurantService;
        this.restaurantOrderService = restaurantOrderService;
    }

    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<RestaurantDto> getRestaurantById(@PathVariable UUID id) {
        Restaurant restaurant = restaurantService.getById(new RestaurantId(id));
        if (restaurant == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(RestaurantDto.from(restaurant));
    }

    @GetMapping({"", "/"})
    @PermitAll
    public ResponseEntity<Collection<RestaurantDto>> getAllRestaurants() {
        return ResponseEntity.ok(
                restaurantService.getAll().stream()
                        .map(RestaurantDto::from)
                        .toList()
        );
    }

    @PostMapping({"", "/"})
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<RestaurantDto> createRestaurant(@Valid @RequestBody CreateRestaurantDto dto, @AuthenticationPrincipal Jwt principal) {

        UUID ownerId = UUID.fromString(principal.getSubject());

        Address address = new Address(dto.address().street(), dto.address().houseNumber(), dto.address().busNumber(), dto.address().postalCode(), dto.address().city(), dto.address().country());

        Restaurant created = restaurantService.create(ownerId,
                dto.name(), address,
                dto.phoneNumber(),
                dto.email(),
                dto.cuisineType(),
                dto.openingHours()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId().id()).toUri();

        return ResponseEntity.created(location).body(RestaurantDto.from(created));
    }

    @GetMapping("/{id}/open-status")
    @PermitAll
    public ResponseEntity<RestaurantOpenStatusDto> isOpen(@PathVariable UUID id) {
        Restaurant restaurant = restaurantService.getById(new RestaurantId(id));
        if (restaurant == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new RestaurantOpenStatusDto(restaurant.isCurrentlyOpen()));
    }

    @PostMapping("/{restaurantId}/dishes")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<DishDto> addDish(@PathVariable UUID restaurantId,
                                           @Valid @RequestBody CreateDishDto dto) {

        Dish dish = restaurantService.addDish(
                new RestaurantId(restaurantId),
                dto.name(),
                dto.description(),
                dto.price(),
                dto.vegetarian(),
                dto.vegan(),
                dto.glutenFree(),
                dto.category(),
                dto.status()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(dish.getId().id()).toUri();

        return ResponseEntity.created(location).body(DishDto.from(dish));
    }

    @PutMapping("/{restaurantId}/dishes/{dishId}")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<DishDto> updateDish(@PathVariable UUID restaurantId,
                                              @PathVariable UUID dishId,
                                              @Valid @RequestBody UpdateDishDto dto) {

        DishPendingUpdate update = new DishPendingUpdate(
                dto.name(),
                dto.description(),
                dto.price(),
                dto.vegetarian(),
                dto.vegan(),
                dto.glutenFree(),
                dto.category(),
                dto.status(),
                dto.scheduledFor()
        );

        Dish updated = restaurantService.updateDish(new RestaurantId(restaurantId), new DishId(dishId), update);

        return ResponseEntity.ok(DishDto.from(updated));
    }

    @PatchMapping("/{restaurantId}/dishes/{dishId}/status")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<DishDto> changeDishStatus(@PathVariable UUID restaurantId,
                                                    @PathVariable UUID dishId,
                                                    @RequestParam String status) {

        Dish updatedDish = restaurantService.updateDishStatus(
                new RestaurantId(restaurantId),
                new DishId(dishId),
                status
        );
        return ResponseEntity.ok(DishDto.from(updatedDish));
    }

    @DeleteMapping("/{restaurantId}/dishes/{dishId}")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<Void> removeDish(@PathVariable UUID restaurantId,
                                           @PathVariable UUID dishId) {

        restaurantService.removeDish(new RestaurantId(restaurantId), new DishId(dishId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{restaurantId}/dishes/publish")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<Void> publishAllDishes(@PathVariable UUID restaurantId) {
        restaurantService.publishAllDishes(new RestaurantId(restaurantId));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status-override")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<Void> overrideOpeningStatus(@PathVariable UUID id, @Valid @RequestBody OverrideOpeningDto dto) {
        restaurantService.overrideOpeningHours(new RestaurantId(id), dto.open(), dto.until());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/status-override")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<Void> clearOverride(@PathVariable UUID id) {
        restaurantService.clearOverride(new RestaurantId(id));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/opening-hours")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<Void> updateOpeningHours(@PathVariable UUID id, @Valid @RequestBody CreateOpeningHoursDto dto) {
        restaurantService.updateOpeningHours(new RestaurantId(id), dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/dishes/{dishId}")
    @PermitAll
    public ResponseEntity<DishForOrderingDto> getDish(@PathVariable UUID id, @PathVariable UUID dishId) {
        DishForOrderingDto dishDto = restaurantService.getRestaurantDishes(new RestaurantId(id), new DishId(dishId));
        return ResponseEntity.ok(dishDto);
    }

    @GetMapping("/{id}/dishes")
    @PermitAll
    public ResponseEntity<Collection<DishDto>> getDishes(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean includeConcepts) {

        var dishes = includeConcepts ? restaurantService.getAllDishesFromRestaurant(new RestaurantId(id)) : restaurantService.getNonConceptDishesFromRestaurant(new RestaurantId(id));

        return ResponseEntity.ok(dishes.stream().map(DishDto::from).toList());
    }

    @GetMapping("/{restaurantId}/orders")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<Collection<RestaurantOrderDto>> getPendingOrders(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(restaurantOrderService.getPendingOrders(restaurantId));
    }

    @GetMapping("/{restaurantId}/orders/{orderId}")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<RestaurantOrderDto> getOrder(@PathVariable UUID restaurantId, @PathVariable UUID orderId) {
        return restaurantOrderService.getOrder(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{restaurantId}/orders/{orderId}/decision")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<Void> decideOrder(@PathVariable UUID restaurantId,
                                            @PathVariable UUID orderId,
                                            @RequestBody DecisionDto dto) {
        boolean applied = restaurantOrderService.applyDecision(orderId, dto.decision(), dto.reason());
        return applied ? ResponseEntity.noContent().build() : ResponseEntity.ok().build();
    }

    @PostMapping("/{restaurantId}/orders/{orderId}/ready")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<Void> readyOrder(@PathVariable UUID restaurantId,
                                           @PathVariable UUID orderId) {
        boolean applied = restaurantOrderService.markReadyForPickup(orderId);
        return applied ? ResponseEntity.noContent().build() : ResponseEntity.ok().build();
    }
}
