package be.kdg.sa.backend.application.services.restaurant;

import be.kdg.sa.backend.api.dto.dish.DishForOrderingDto;
import be.kdg.sa.backend.api.dto.openingHours.CreateOpeningDayDto;
import be.kdg.sa.backend.api.dto.openingHours.CreateOpeningHoursDto;
import be.kdg.sa.backend.api.dto.openingHours.CreateOpeningPeriodDto;
import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.NotFoundException;
import be.kdg.sa.backend.domain.owner.OwnerId;
import be.kdg.sa.backend.domain.restaurant.Restaurant;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import be.kdg.sa.backend.domain.restaurant.dish.Dish;
import be.kdg.sa.backend.domain.restaurant.dish.DishCategory;
import be.kdg.sa.backend.domain.restaurant.dish.DishPendingUpdate;
import be.kdg.sa.backend.domain.restaurant.dish.DishStatus;
import be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository.RestaurantDb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantDb restaurantRepository;

    @InjectMocks
    private RestaurantService sut;

    private Restaurant restaurant;
    private RestaurantId restaurantId;

    @BeforeEach
    void setup() {
        UUID ownerId = UUID.randomUUID();
        restaurantId = RestaurantId.create();
        restaurant = Restaurant.create(
                "MyRestaurant",
                new Address("Street", "1", "A", "1000", "City", "Belgium"),
                "123",
                "email@test.com",
                "Italian",
                new OwnerId(ownerId)
        );
    }

    @Test
    void getById_returnsRestaurant() {
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        Restaurant result = sut.getById(restaurantId);
        assertThat(result).isEqualTo(restaurant);
    }

    @Test
    void getById_throwsIfNotFound() {
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> sut.getById(restaurantId));
    }

    @Test
    void getAll_returnsAllRestaurants() {
        given(restaurantRepository.findAll()).willReturn(List.of(restaurant));
        Collection<Restaurant> results = sut.getAll();
        assertThat(results).containsExactly(restaurant);
    }

    @Test
    void addDish_addsDishAndSaves() {
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        Dish dish = sut.addDish(restaurantId, "Pasta", "Tasty", 10, true, false, false, DishCategory.MAIN, DishStatus.AVAILABLE);

        assertThat(dish.getName()).isEqualTo("Pasta");
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void addDish_throwsIfRestaurantNotFound() {
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> sut.addDish(restaurantId, "Pasta", "Desc", 10, true, false, false, DishCategory.MAIN, DishStatus.AVAILABLE));
    }

    @Test
    void updateDish_appliesPendingUpdate() {
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        Dish dish = restaurant.addDish("OldName", "OldDesc", 5, true, false, false, DishCategory.MAIN, DishStatus.AVAILABLE);

        DishPendingUpdate dto = new DishPendingUpdate("NewName", "NewDesc", 7.5, null, null, null, null, null, LocalDateTime.now());
        Dish updated = sut.updateDish(restaurantId, dish.getId(), dto);

        assertThat(updated.getName()).isEqualTo("NewName");
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void create_restaurant_createsWithoutOwnerEntity() {
        UUID ownerUuid = UUID.randomUUID();

        CreateOpeningPeriodDto period = new CreateOpeningPeriodDto(LocalTime.of(9, 0), LocalTime.of(17, 0));
        CreateOpeningDayDto day = new CreateOpeningDayDto(DayOfWeek.MONDAY, List.of(period));
        CreateOpeningHoursDto openingHoursDto = new CreateOpeningHoursDto(List.of(day));

        Restaurant created = sut.create(
                ownerUuid,
                "NewResto",
                new Address("Street", "1", "A", "1000", "City", "Belgium"),
                "123",
                "email@test.com",
                "Italian",
                openingHoursDto
        );

        assertThat(created.getName()).isEqualTo("NewResto");
        verify(restaurantRepository).save(created);
    }

    @Test
    void updateDishStatus_updatesStatus() {
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        Dish dish = restaurant.addDish("Pasta", "desc", 10, false, false, false, DishCategory.MAIN, DishStatus.AVAILABLE);

        Dish updated = sut.updateDishStatus(restaurantId, dish.getId(), "CONCEPT");

        assertThat(updated.getStatus()).isEqualTo(DishStatus.CONCEPT);
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void removeDish_removesDish() {
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        Dish dish = restaurant.addDish("Pizza", "desc", 10, false, false, false, DishCategory.MAIN, DishStatus.AVAILABLE);

        sut.removeDish(restaurantId, dish.getId());
        assertThat(restaurant.getNonConceptDishes()).doesNotContain(dish);
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void publishAllDishes_callsPublish() {
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        sut.publishAllDishes(restaurantId);
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void overrideOpeningHours_and_clearOverride() {
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        sut.overrideOpeningHours(restaurantId, true, LocalDateTime.now().plusDays(1));
        assertThat(restaurant.isCurrentlyOpen()).isTrue();

        sut.clearOverride(restaurantId);
        verify(restaurantRepository, times(2)).save(restaurant);
    }

    @Test
    void updateOpeningHours_updates() {
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        CreateOpeningPeriodDto period = new CreateOpeningPeriodDto(LocalTime.of(9, 0), LocalTime.of(17, 0));
        CreateOpeningDayDto day = new CreateOpeningDayDto(DayOfWeek.MONDAY, List.of(period));
        CreateOpeningHoursDto dto = new CreateOpeningHoursDto(List.of(day));

        sut.updateOpeningHours(restaurantId, dto);
        verify(restaurantRepository).save(restaurant);
    }

    @Test
    void getRestaurantDishes_returnsDto() {
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        Dish dish = restaurant.addDish("Pasta", "desc", 10, false, false, false, DishCategory.MAIN, DishStatus.AVAILABLE);

        DishForOrderingDto dto = sut.getRestaurantDishes(restaurantId, dish.getId());
        assertThat(dto.name()).isEqualTo("Pasta");
    }

    @Test
    void getAllDishesFromRestaurant_returnsAllDishes() {
        given(restaurantRepository.findAllDishesByRestaurantId(restaurantId)).willReturn(List.of());
        Collection<Dish> dishes = sut.getAllDishesFromRestaurant(restaurantId);
        assertThat(dishes).isEmpty();
    }

    @Test
    void getNonConceptDishesFromRestaurant_returnsDishesOrThrows() {
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        Collection<Dish> nonConcept = sut.getNonConceptDishesFromRestaurant(restaurantId);
        assertThat(nonConcept).isEmpty();

        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> sut.getNonConceptDishesFromRestaurant(restaurantId));
    }
}
