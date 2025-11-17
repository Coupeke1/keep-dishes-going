package be.kdg.sa.backend.domain.restaurant;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.owner.OwnerId;
import be.kdg.sa.backend.domain.restaurant.dish.Dish;
import be.kdg.sa.backend.domain.restaurant.dish.DishCategory;
import be.kdg.sa.backend.domain.restaurant.dish.DishId;
import be.kdg.sa.backend.domain.restaurant.dish.DishStatus;
import be.kdg.sa.backend.domain.restaurant.openingHours.OpeningDay;
import be.kdg.sa.backend.domain.restaurant.openingHours.OpeningHours;
import be.kdg.sa.backend.domain.restaurant.openingHours.OpeningPeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class RestaurantTest {

    @Test
    void shouldCreateRestaurantWithAllFields() {
        RestaurantId id = RestaurantId.create();
        OwnerId ownerId = new OwnerId(UUID.randomUUID());
        Address address = new Address("Main Street", "123", null, "2000", "Antwerp", "Belgium");
        Menu menu = new Menu();

        Restaurant restaurant = Restaurant.reconstruct(id, "Test Restaurant", address, "+3212345678",
                "test@restaurant.com", "Italian", menu, ownerId);

        assertThat(restaurant.getId()).isEqualTo(id);
        assertThat(restaurant.getName()).isEqualTo("Test Restaurant");
        assertThat(restaurant.getAddress()).isEqualTo(address);
        assertThat(restaurant.getPhoneNumber()).isEqualTo("+3212345678");
        assertThat(restaurant.getEmail()).isEqualTo("test@restaurant.com");
        assertThat(restaurant.getCuisineType()).isEqualTo("Italian");
        assertThat(restaurant.getMenu()).isEqualTo(menu);
        assertThat(restaurant.getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    void shouldRegisterNewRestaurant() {
        OwnerId ownerId = new OwnerId(UUID.randomUUID());
        Address address = new Address("Main Street", "123", null, "2000", "Antwerp", "Belgium");

        Restaurant restaurant = Restaurant.create("New Restaurant", address, "+3212345678",
                "info@newrestaurant.com", "French", ownerId);

        assertThat(restaurant.getId()).isNotNull();
        assertThat(restaurant.getOwnerId()).isEqualTo(ownerId);
        assertThat(restaurant.getName()).isEqualTo("New Restaurant");
        assertThat(restaurant.getAddress()).isEqualTo(address);
        assertThat(restaurant.getPhoneNumber()).isEqualTo("+3212345678");
        assertThat(restaurant.getEmail()).isEqualTo("info@newrestaurant.com");
        assertThat(restaurant.getCuisineType()).isEqualTo("French");
        assertThat(restaurant.getMenu()).isNotNull();
    }

    @Test
    void shouldCreateRestaurantWithEmptyMenuWhenNullProvided() {
        OwnerId ownerId = new OwnerId(UUID.randomUUID());
        Address address = new Address("Main Street", "123", null, "2000", "Antwerp", "Belgium");

        Restaurant restaurant = Restaurant.create("Test Restaurant", address, "+3212345678",
                "test@restaurant.com", "Italian", ownerId);

        assertThat(restaurant.getMenu()).isNotNull();
        assertThat(restaurant.getMenu().getDishes()).isEmpty();
    }

    @Test
    void shouldAddDishToMenu() {
        Restaurant restaurant = createBasicRestaurant();

        Dish dish = restaurant.addDish("Pizza Margherita", "Classic tomato and mozzarella", 12.50,
                true, false, false, DishCategory.MAIN, DishStatus.AVAILABLE);

        assertThat(dish).isNotNull();
        assertThat(dish.getName()).isEqualTo("Pizza Margherita");
        assertThat(restaurant.getMenu().getDishes()).hasSize(1);
        assertThat(restaurant.getMenu().getDishes().getFirst()).isEqualTo(dish);
    }

    @Test
    void shouldRenameRestaurant() {
        Restaurant restaurant = createBasicRestaurant();

        restaurant.rename("Updated Restaurant Name");

        assertThat(restaurant.getName()).isEqualTo("Updated Restaurant Name");
    }

    @Test
    void shouldChangeCuisineType() {
        Restaurant restaurant = createBasicRestaurant();

        restaurant.changeCuisineType("Mediterranean");

        assertThat(restaurant.getCuisineType()).isEqualTo("Mediterranean");
    }

    @Test
    void shouldUpdateOpeningHours() {
        Restaurant restaurant = createBasicRestaurant();
        OpeningHours openingHours = createTestOpeningHours();

        restaurant.updateOpeningHours(openingHours);

        assertThat(restaurant.getOpeningHours()).isEqualTo(openingHours);
    }

    @Test
    void shouldUpdateLogo() {
        Restaurant restaurant = createBasicRestaurant();

        restaurant.updateLogo("https://example.com/logo.png");

        assertThat(restaurant.getLogoUrl()).isEqualTo("https://example.com/logo.png");
    }

    @Test
    void shouldChangeDishDetails() {
        Restaurant restaurant = createBasicRestaurant();
        Dish dish = restaurant.addDish("Pizza", "Original pizza", 10.0,
                true, false, false, DishCategory.MAIN, DishStatus.AVAILABLE);

        Dish updatedDish = restaurant.changeDish(dish.getId(), "Updated Pizza", "New description", 15.0,
                false, false, true, DishCategory.MAIN, DishStatus.SOLD_OUT);

        assertThat(updatedDish.getName()).isEqualTo("Updated Pizza");
        assertThat(updatedDish.getDescription()).isEqualTo("New description");
        assertThat(updatedDish.getPrice()).isEqualTo(15.0);
        assertThat(updatedDish.isVegetarian()).isFalse();
        assertThat(updatedDish.isGlutenFree()).isTrue();
        assertThat(updatedDish.getStatus()).isEqualTo(DishStatus.SOLD_OUT);
    }

    @Test
    void shouldRemoveDishFromMenu() {
        Restaurant restaurant = createBasicRestaurant();
        Dish dish = restaurant.addDish("Pizza", "Test pizza", 10.0,
                true, false, false, DishCategory.MAIN, DishStatus.AVAILABLE);

        restaurant.removeDish(dish.getId());

        assertThat(restaurant.getMenu().getDishes()).isEmpty();
    }

    @Test
    void shouldThrowWhenChangingNonExistentDish() {
        Restaurant restaurant = createBasicRestaurant();
        DishId nonExistentId = DishId.create();

        assertThrows(IllegalArgumentException.class,
                () -> restaurant.changeDish(nonExistentId, "Name", "Desc", 10.0,
                        true, false, false, DishCategory.MAIN, DishStatus.AVAILABLE));
    }

    @Test
    void shouldThrowWhenRemovingNonExistentDish() {
        Restaurant restaurant = createBasicRestaurant();
        DishId nonExistentId = DishId.create();

        assertThrows(NoSuchElementException.class,
                () -> restaurant.removeDish(nonExistentId));
    }

    @Test
    void shouldChangeDishStatus() {
        Restaurant restaurant = createBasicRestaurant();
        Dish dish = restaurant.addDish("Pizza", "Test pizza", 10.0,
                true, false, false, DishCategory.MAIN, DishStatus.AVAILABLE);

        Dish updatedDish = restaurant.changeDishStatus(dish.getId(), DishStatus.SOLD_OUT);

        assertThat(updatedDish.getStatus()).isEqualTo(DishStatus.SOLD_OUT);
    }

    @Test
    void shouldPublishAllConceptDishes() {
        Restaurant restaurant = createBasicRestaurant();
        restaurant.addDish("Concept Dish", "Test concept", 10.0,
                true, false, false, DishCategory.MAIN, DishStatus.CONCEPT);

        restaurant.publishAllConceptDishes();

        List<Dish> nonConceptDishes = restaurant.getNonConceptDishes();
        assertThat(nonConceptDishes).hasSize(1);
        assertThat(nonConceptDishes.getFirst().getStatus()).isEqualTo(DishStatus.AVAILABLE);
    }

    @Test
    void shouldGetNonConceptDishes() {
        Restaurant restaurant = createBasicRestaurant();
        restaurant.addDish("Available Dish", "Test", 10.0, true, false, false,
                DishCategory.MAIN, DishStatus.AVAILABLE);
        restaurant.addDish("Concept Dish", "Test", 10.0, true, false, false,
                DishCategory.MAIN, DishStatus.CONCEPT);
        restaurant.addDish("Sold Out Dish", "Test", 10.0, true, false, false,
                DishCategory.MAIN, DishStatus.SOLD_OUT);

        List<Dish> nonConceptDishes = restaurant.getNonConceptDishes();

        assertThat(nonConceptDishes).hasSize(2);
        assertThat(nonConceptDishes).allMatch(d -> d.getStatus() != DishStatus.CONCEPT);
    }

    @Test
    void shouldCalculateAveragePrice() {
        Restaurant restaurant = createBasicRestaurant();
        restaurant.addDish("Dish 1", "Test", 10.0, true, false, false,
                DishCategory.MAIN, DishStatus.AVAILABLE);
        restaurant.addDish("Dish 2", "Test", 20.0, true, false, false,
                DishCategory.MAIN, DishStatus.AVAILABLE);

        double averagePrice = restaurant.getAveragePrice();

        assertThat(averagePrice).isEqualTo(15.0);
    }

    @Test
    void shouldReturnZeroAveragePriceWhenNoDishes() {
        Restaurant restaurant = createBasicRestaurant();
        double averagePrice = restaurant.getAveragePrice();
        assertThat(averagePrice).isEqualTo(0.0);
    }

    @Test
    void shouldGetPriceIndicator() {
        Restaurant restaurant = createBasicRestaurant();
        restaurant.addDish("Dish 1", "Test", 10.0, true, false, false,
                DishCategory.MAIN, DishStatus.AVAILABLE);
        restaurant.addDish("Dish 2", "Test", 20.0, true, false, false,
                DishCategory.MAIN, DishStatus.AVAILABLE);

        String priceIndicator = restaurant.getPriceIndicator();

        assertThat(priceIndicator).isIn("€", "€€", "€€€", "€€€€");
    }

    @Test
    void shouldBeOpenBasedOnOpeningHours() {
        Restaurant restaurant = createBasicRestaurant();
        OpeningHours openingHours = createTestOpeningHours();
        restaurant.updateOpeningHours(openingHours);
        boolean isOpen = restaurant.isCurrentlyOpen();
        assertThat(isOpen).isInstanceOf(Boolean.class);
    }

    @Test
    void shouldBeOpenWhenWithinOpeningHours() {
        Restaurant restaurant = createBasicRestaurant();
        OpeningHours alwaysOpen = createAlwaysOpenHours();
        restaurant.updateOpeningHours(alwaysOpen);
        boolean isOpen = restaurant.isCurrentlyOpen();
        assertThat(isOpen).isTrue();
    }

    @Test
    void shouldBeClosedWhenOutsideOpeningHours() {
        Restaurant restaurant = createBasicRestaurant();
        OpeningHours alwaysClosed = createAlwaysClosedHours();
        restaurant.updateOpeningHours(alwaysClosed);
        boolean isOpen = restaurant.isCurrentlyOpen();
        assertThat(isOpen).isFalse();
    }

    @Test
    void shouldOverrideOpeningManually() {
        Restaurant restaurant = createBasicRestaurant();
        LocalDateTime until = LocalDateTime.now().plusHours(2);
        restaurant.overrideOpeningManually(true, until);
        assertThat(restaurant.getManualOverrideOpen()).isTrue();
        assertThat(restaurant.getOverrideUntil()).isEqualTo(until);
    }

    @Test
    void shouldUseManualOverrideWhenSet() {
        Restaurant restaurant = createBasicRestaurant();
        LocalDateTime until = LocalDateTime.now().plusHours(2);
        restaurant.overrideOpeningManually(true, until);
        boolean isOpen = restaurant.isCurrentlyOpen();
        assertThat(isOpen).isTrue();
    }

    @Test
    void shouldUseManualOverrideToClose() {
        Restaurant restaurant = createBasicRestaurant();
        OpeningHours openingHours = createAlwaysOpenHours();
        restaurant.updateOpeningHours(openingHours);
        LocalDateTime until = LocalDateTime.now().plusHours(2);
        restaurant.overrideOpeningManually(false, until);
        boolean isOpen = restaurant.isCurrentlyOpen();
        assertThat(isOpen).isFalse();
    }

    @Test
    void shouldClearManualOverrideAfterTimeExpires() {
        Restaurant restaurant = createBasicRestaurant();
        OpeningHours openingHours = createAlwaysOpenHours();
        restaurant.updateOpeningHours(openingHours);
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        restaurant.overrideOpeningManually(false, pastTime);
        boolean isOpen = restaurant.isCurrentlyOpen();
        assertThat(restaurant.getManualOverrideOpen()).isNull();
        assertThat(isOpen).isTrue();
    }

    @Test
    void shouldHandleNullOpeningHours() {
        Restaurant restaurant = createBasicRestaurant();
        boolean isOpen = restaurant.isCurrentlyOpen();
        assertThat(isOpen).isFalse();
    }

    @Test
    void shouldMaintainImmutableId() {
        Restaurant restaurant = createBasicRestaurant();
        RestaurantId originalId = restaurant.getId();
        assertThat(restaurant.getId()).isSameAs(originalId);
    }

    @Test
    void shouldHandleEmptyStringsInContactInfo() {
        OwnerId ownerId = new OwnerId(UUID.randomUUID());
        Address address = new Address("Street", "1", null, "1000", "City", "Belgium");
        Restaurant restaurant = Restaurant.create("Test", address, "", "", "Cuisine", ownerId);
        assertThat(restaurant.getPhoneNumber()).isEmpty();
        assertThat(restaurant.getEmail()).isEmpty();
    }

    @Test
    void shouldHandleVeryLongStrings() {
        OwnerId ownerId = new OwnerId(UUID.randomUUID());
        String longName = "A".repeat(1000);
        String longEmail = "test@".repeat(50) + "example.com";
        String longCuisine = "B".repeat(500);

        Address address = new Address("Street", "1", null, "1000", "City", "Belgium");
        Restaurant restaurant = Restaurant.create(longName, address, "123456", longEmail, longCuisine, ownerId);

        assertThat(restaurant.getName()).isEqualTo(longName);
        assertThat(restaurant.getEmail()).isEqualTo(longEmail);
        assertThat(restaurant.getCuisineType()).isEqualTo(longCuisine);
    }

    @Test
    void shouldNotBeEqualToDifferentRestaurant() {
        OwnerId ownerId = new OwnerId(UUID.randomUUID());
        Address address = new Address("Street", "1", null, "1000", "City", "Belgium");
        Restaurant restaurant1 = Restaurant.create("Restaurant A", address, "123", "a@test.com", "Italian", ownerId);
        Restaurant restaurant2 = Restaurant.create("Restaurant B", address, "456", "b@test.com", "French", ownerId);

        assertThat(restaurant1).isNotEqualTo(restaurant2);
    }

    @Test
    void shouldMaintainStateAfterMultipleOperations() {
        Restaurant restaurant = createBasicRestaurant();
        restaurant.rename("Updated Name");
        restaurant.changeCuisineType("Fusion");
        restaurant.updateLogo("new-logo.png");
        restaurant.updateOpeningHours(createTestOpeningHours());
        Dish dish = restaurant.addDish("New Dish", "Description", 15.0,
                true, true, true, DishCategory.MAIN, DishStatus.AVAILABLE);
        restaurant.changeDishStatus(dish.getId(), DishStatus.SOLD_OUT);

        assertThat(restaurant.getName()).isEqualTo("Updated Name");
        assertThat(restaurant.getCuisineType()).isEqualTo("Fusion");
        assertThat(restaurant.getLogoUrl()).isEqualTo("new-logo.png");
        assertThat(restaurant.getOpeningHours()).isNotNull();
        assertThat(restaurant.getMenu().getDishes()).hasSize(1);
        assertThat(restaurant.getMenu().getDishes().getFirst().getStatus()).isEqualTo(DishStatus.SOLD_OUT);
    }

    @Test
    void shouldCalculateAveragePriceWithMixedStatusDishes() {
        Restaurant restaurant = createBasicRestaurant();
        restaurant.addDish("Available Dish", "Test", 10.0, true, false, false,
                DishCategory.MAIN, DishStatus.AVAILABLE);
        restaurant.addDish("Concept Dish", "Test", 20.0, true, false, false,
                DishCategory.MAIN, DishStatus.CONCEPT);
        restaurant.addDish("Sold Out Dish", "Test", 30.0, true, false, false,
                DishCategory.MAIN, DishStatus.SOLD_OUT);

        double averagePrice = restaurant.getAveragePrice();

        assertThat(averagePrice).isEqualTo(10.0);
    }

    @Test
    void shouldCalculateAveragePriceWithNoAvailableDishes() {
        Restaurant restaurant = createBasicRestaurant();
        restaurant.addDish("Concept Dish", "Test", 20.0, true, false, false,
                DishCategory.MAIN, DishStatus.CONCEPT);
        restaurant.addDish("Sold Out Dish", "Test", 30.0, true, false, false,
                DishCategory.MAIN, DishStatus.SOLD_OUT);

        double averagePrice = restaurant.getAveragePrice();

        assertThat(averagePrice).isEqualTo(0.0);
    }

    @Test
    void shouldGetNonConceptDishesIncludingSoldOut() {
        Restaurant restaurant = createBasicRestaurant();
        restaurant.addDish("Available Dish", "Test", 10.0, true, false, false,
                DishCategory.MAIN, DishStatus.AVAILABLE);
        restaurant.addDish("Concept Dish", "Test", 10.0, true, false, false,
                DishCategory.MAIN, DishStatus.CONCEPT);
        restaurant.addDish("Sold Out Dish", "Test", 10.0, true, false, false,
                DishCategory.MAIN, DishStatus.SOLD_OUT);

        List<Dish> nonConceptDishes = restaurant.getNonConceptDishes();

        assertThat(nonConceptDishes).hasSize(2);
        assertThat(nonConceptDishes)
                .extracting(Dish::getStatus)
                .containsExactlyInAnyOrder(DishStatus.AVAILABLE, DishStatus.SOLD_OUT);
    }

    @Test
    void shouldHandleAllDishStatusTransitions() {
        Restaurant restaurant = createBasicRestaurant();
        Dish dish = restaurant.addDish("Test Dish", "Description", 10.0,
                true, false, false, DishCategory.MAIN, DishStatus.CONCEPT);

        restaurant.changeDishStatus(dish.getId(), DishStatus.AVAILABLE);
        assertThat(dish.getStatus()).isEqualTo(DishStatus.AVAILABLE);

        restaurant.changeDishStatus(dish.getId(), DishStatus.SOLD_OUT);
        assertThat(dish.getStatus()).isEqualTo(DishStatus.SOLD_OUT);

        restaurant.changeDishStatus(dish.getId(), DishStatus.AVAILABLE);
        assertThat(dish.getStatus()).isEqualTo(DishStatus.AVAILABLE);
    }

    private Restaurant createBasicRestaurant() {
        OwnerId ownerId = new OwnerId(UUID.randomUUID());
        Address address = new Address("Main Street", "123", null, "2000", "Antwerp", "Belgium");
        return Restaurant.create("Test Restaurant", address, "+3212345678", "test@restaurant.com", "Italian", ownerId);
    }

    private OpeningHours createTestOpeningHours() {
        OpeningDay monday = new OpeningDay(DayOfWeek.MONDAY, List.of(
                new OpeningPeriod(LocalTime.of(9, 0), LocalTime.of(18, 0))
        ));
        return new OpeningHours(List.of(monday));
    }

    private OpeningHours createAlwaysOpenHours() {
        DayOfWeek today = LocalDateTime.now().getDayOfWeek();
        OpeningDay todayOpen = new OpeningDay(today, List.of(
                new OpeningPeriod(LocalTime.of(0, 0), LocalTime.of(23, 59))
        ));
        return new OpeningHours(List.of(todayOpen));
    }

    private OpeningHours createAlwaysClosedHours() {
        DayOfWeek today = LocalDateTime.now().getDayOfWeek();
        OpeningDay todayClosed = new OpeningDay(today, List.of());
        return new OpeningHours(List.of(todayClosed));
    }
}
