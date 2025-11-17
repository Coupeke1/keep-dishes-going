package be.kdg.sa.backend.application.services.restaurant;

import be.kdg.sa.backend.api.dto.dish.DishForOrderingDto;
import be.kdg.sa.backend.api.dto.openingHours.CreateOpeningHoursDto;
import be.kdg.sa.backend.api.dto.owner.OwnerOverviewDto;
import be.kdg.sa.backend.api.dto.restaurant.DishSummaryDto;
import be.kdg.sa.backend.api.dto.restaurant.RestaurantOverviewDto;
import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.NotFoundException;
import be.kdg.sa.backend.domain.owner.OwnerId;
import be.kdg.sa.backend.domain.restaurant.Restaurant;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import be.kdg.sa.backend.domain.restaurant.dish.*;
import be.kdg.sa.backend.domain.restaurant.openingHours.OpeningDay;
import be.kdg.sa.backend.domain.restaurant.openingHours.OpeningHours;
import be.kdg.sa.backend.domain.restaurant.openingHours.OpeningPeriod;
import be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository.RestaurantRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Transactional
@Service
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public Restaurant getById(RestaurantId id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("restaurant not found: " + id));
    }

    public Collection<Restaurant> getAll() {
        return restaurantRepository.findAll();
    }

    public Restaurant create(UUID ownerId, String name, Address address, String phone, String email,
                             String cuisineType, CreateOpeningHoursDto createOpeningHoursDto) {

        boolean alreadyExists = restaurantRepository.existsByOwnerId(new OwnerId(ownerId));
        if (alreadyExists) {
            throw new IllegalStateException("Owner already has a restaurant registered.");
        }

        OpeningHours openingHours = null;
        if (createOpeningHoursDto != null) {
            openingHours = new OpeningHours(
                    createOpeningHoursDto.days().stream()
                            .map(dayDto -> new OpeningDay(
                                    dayDto.day(),
                                    dayDto.periods().stream()
                                            .map(p -> new OpeningPeriod(p.openTime(), p.closeTime()))
                                            .toList()
                            ))
                            .toList()
            );
        }

        Restaurant restaurant = Restaurant.create(
                name,
                address,
                phone,
                email,
                cuisineType,
                new OwnerId(ownerId)
        );

        if (openingHours != null) {
            restaurant.updateOpeningHours(openingHours);
        }

        restaurantRepository.save(restaurant);
        return restaurant;
    }

    public Restaurant getByOwner(UUID ownerId) {
        return restaurantRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new NotFoundException("No restaurant found for owner " + ownerId));
    }

    public Dish addDish(RestaurantId restaurantId,
                        @NotBlank(message = "Name is required") String name,
                        String description, double price, boolean vegetarian, boolean vegan,
                        boolean glutenFree, @NotNull(message = "Category is required") DishCategory category,
                        @NotNull(message = "Status is required") DishStatus status) {
        Restaurant restaurant = getById(restaurantId);
        Dish dish = restaurant.addDish(name, description, price, vegetarian, vegan, glutenFree, category, status);
        restaurantRepository.save(restaurant);
        return dish;
    }

    public Dish updateDish(RestaurantId restaurantId, DishId dishId, DishPendingUpdate dto) {
        Restaurant restaurant = getById(restaurantId);
        Dish dish = restaurant.getMenu().getDishes().stream()
                .filter(d -> d.getId().equals(dishId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Dish not found: " + dishId));

        dish.scheduleOrApplyUpdate(dto);
        restaurantRepository.save(restaurant);
        return dish;
    }

    public Dish updateDishStatus(RestaurantId restaurantId, DishId dishId, String status) {
        Restaurant restaurant = getById(restaurantId);
        DishStatus dishStatus = DishStatus.valueOf(status);
        Dish updatedDish = restaurant.changeDishStatus(dishId, dishStatus);
        restaurantRepository.save(restaurant);
        return updatedDish;
    }

    public void removeDish(RestaurantId restaurantId, DishId dishId) {
        Restaurant restaurant = getById(restaurantId);
        restaurant.removeDish(dishId);
        restaurantRepository.save(restaurant);
    }

    public void publishAllDishes(RestaurantId restaurantId) {
        Restaurant restaurant = getById(restaurantId);
        restaurant.publishAllConceptDishes();
        restaurantRepository.save(restaurant);
    }

    public void overrideOpeningHours(RestaurantId restaurantId, boolean open, LocalDateTime until) {
        Restaurant restaurant = getById(restaurantId);
        restaurant.overrideOpeningManually(open, until);
        restaurantRepository.save(restaurant);
    }

    public void clearOverride(RestaurantId restaurantId) {
        Restaurant restaurant = getById(restaurantId);
        restaurant.overrideOpeningManually(false, null);
        restaurantRepository.save(restaurant);
    }

    public void updateOpeningHours(RestaurantId id, CreateOpeningHoursDto dto) {
        Restaurant restaurant = getById(id);
        OpeningHours openingHours = new OpeningHours(
                dto.days().stream()
                        .map(dayDto -> new OpeningDay(
                                dayDto.day(),
                                dayDto.periods().stream()
                                        .map(p -> new OpeningPeriod(p.openTime(), p.closeTime()))
                                        .toList()
                        ))
                        .toList()
        );
        restaurant.updateOpeningHours(openingHours);
        restaurantRepository.save(restaurant);
    }

    public void applyScheduledDishChanges() {
        restaurantRepository.findAll().forEach(restaurant -> {
            boolean changed = restaurant.getMenu().getDishes().stream()
                    .filter(Dish::isUpdateReadyToApply)
                    .peek(Dish::applyPendingChanges)
                    .findAny()
                    .isPresent();

            if (changed) {
                restaurantRepository.save(restaurant);
            }
        });
    }

    public DishForOrderingDto getRestaurantDishes(RestaurantId restaurantId, DishId dishId) {
        Restaurant restaurant = getById(restaurantId);
        return restaurant.getMenu().getDishes().stream()
                .filter(d -> d.getId().equals(dishId))
                .map(DishForOrderingDto::from)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("dish not found: " + dishId));
    }

    public Collection<Dish> getAllDishesFromRestaurant(RestaurantId id) {
        return restaurantRepository.findAllDishesByRestaurantId(id);
    }

    public Collection<Dish> getNonConceptDishesFromRestaurant(RestaurantId id) {
        return restaurantRepository.findById(id)
                .map(Restaurant::getNonConceptDishes)
                .orElseThrow(() -> new NotFoundException("restaurant not found: " + id));
    }

    public OwnerOverviewDto getOverviewForOwner(UUID ownerId) {
        Restaurant restaurant = getByOwner(ownerId);

        List<DishSummaryDto> dishes = restaurant.getMenu().getDishes()
                .stream()
                .map(d -> new DishSummaryDto(
                        d.getId().id(),
                        d.getName(),
                        d.getDescription(),
                        d.getPrice(),
                        d.getStatus(),
                        d.hasPendingChanges(),
                        d.getPendingUpdate() != null ? d.getPendingUpdate().scheduledFor() : null)
                ).toList();

        long pendingCount = dishes.stream().filter(DishSummaryDto::hasPendingUpdate).count();

        RestaurantOverviewDto restaurantOverviewDto = new RestaurantOverviewDto(
                restaurant.getId().id(),
                restaurant.getName(),
                restaurant.isCurrentlyOpen(),
                pendingCount,
                dishes
        );

        return new OwnerOverviewDto(ownerId, restaurantOverviewDto);
    }
}
