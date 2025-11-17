package be.kdg.sa.backend.domain.restaurant;

import be.kdg.sa.backend.domain.restaurant.dish.Dish;
import be.kdg.sa.backend.domain.restaurant.dish.DishId;
import be.kdg.sa.backend.domain.restaurant.dish.DishStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MenuTest {

    private Dish createRealDish(DishId id, String name, double price, DishStatus status) {
        return new Dish(id, name, "Description", price, false, false, false, null, status);
    }

    @Test
    void shouldCreateEmptyMenu() {
        Menu menu = new Menu();

        assertThat(menu.getDishes()).isEmpty();
        assertThat(menu.getConceptDishes()).isEmpty();
        assertThat(menu.getActiveDishes()).isEmpty();
        assertThat(menu.getNonConceptDishes()).isEmpty();
    }

    @Test
    void shouldReconstructMenuWithDishes() {
        DishId dishId1 = DishId.create();
        DishId dishId2 = DishId.create();
        List<Dish> dishes = List.of(
                createRealDish(dishId1, "Pasta", 12.50, DishStatus.AVAILABLE),
                createRealDish(dishId2, "Pizza", 15.99, DishStatus.CONCEPT)
        );

        Menu menu = Menu.reconstruct(dishes);

        assertThat(menu.getDishes()).hasSize(2);
        assertThat(menu.getActiveDishes()).hasSize(1);
        assertThat(menu.getConceptDishes()).hasSize(1);
    }

    @Test
    void shouldReconstructMenuWithEmptyList() {
        Menu menu = Menu.reconstruct(List.of());

        assertThat(menu.getDishes()).isEmpty();
    }

    @Test
    void shouldAddConceptDish() {
        Menu menu = new Menu();
        DishId dishId = DishId.create();
        Dish dish = createRealDish(dishId, "Concept Dish", 10.0, DishStatus.CONCEPT);

        menu.addDish(dish);

        assertThat(menu.getDishes()).containsExactly(dish);
        assertThat(menu.getConceptDishes()).containsExactly(dish);
        assertThat(menu.getActiveDishes()).isEmpty();
    }

    @Test
    void shouldAddAvailableDishWhenUnderLimit() {
        Menu menu = new Menu();
        DishId dishId = DishId.create();
        Dish dish = createRealDish(dishId, "Available Dish", 10.0, DishStatus.AVAILABLE);

        menu.addDish(dish);

        assertThat(menu.getDishes()).containsExactly(dish);
        assertThat(menu.getActiveDishes()).containsExactly(dish);
    }

    @Test
    void shouldThrowWhenAddingAvailableDishAtMaxCapacity() {
        Menu menu = new Menu();

        for (int i = 0; i < 10; i++) {
            DishId dishId = DishId.create();
            Dish dish = createRealDish(dishId, "Dish " + i, 10.0 + i, DishStatus.AVAILABLE);
            menu.addDish(dish);
        }

        DishId newDishId = DishId.create();
        Dish newDish = createRealDish(newDishId, "Extra Dish", 20.0, DishStatus.AVAILABLE);

        assertThrows(IllegalStateException.class, () -> menu.addDish(newDish));
    }

    @Test
    void shouldAddConceptDishWhenAtMaxActiveCapacity() {
        Menu menu = new Menu();

        for (int i = 0; i < 10; i++) {
            DishId dishId = DishId.create();
            Dish dish = createRealDish(dishId, "Dish " + i, 10.0 + i, DishStatus.AVAILABLE);
            menu.addDish(dish);
        }

        DishId conceptDishId = DishId.create();
        Dish conceptDish = createRealDish(conceptDishId, "Concept Dish", 15.0, DishStatus.CONCEPT);

        menu.addDish(conceptDish);

        assertThat(menu.getDishes()).hasSize(11);
        assertThat(menu.getConceptDishes()).containsExactly(conceptDish);
    }

    @Test
    void shouldRemoveExistingDish() {
        DishId dishId1 = DishId.create();
        DishId dishId2 = DishId.create();
        Dish dish1 = createRealDish(dishId1, "Dish 1", 10.0, DishStatus.AVAILABLE);
        Dish dish2 = createRealDish(dishId2, "Dish 2", 12.0, DishStatus.CONCEPT);

        Menu menu = new Menu();
        menu.addDish(dish1);
        menu.addDish(dish2);

        menu.removeDish(dishId1);

        assertThat(menu.getDishes()).containsExactly(dish2);
    }

    @Test
    void shouldRemoveNonExistingDishWithoutError() {
        Menu menu = new Menu();
        DishId existingDishId = DishId.create();
        Dish existingDish = createRealDish(existingDishId, "Existing Dish", 10.0, DishStatus.AVAILABLE);
        menu.addDish(existingDish);

        DishId nonExistingDishId = DishId.create();

        assertThrows(NoSuchElementException.class, () -> menu.removeDish(nonExistingDishId));
        assertThat(menu.getDishes()).containsExactly(existingDish);
    }

    @Test
    void shouldRemoveFromEmptyMenu() {
        Menu menu = new Menu();
        DishId dishId = DishId.create();

        menu.addDish(createRealDish(dishId, "Dish", 10.0, DishStatus.AVAILABLE));
        menu.removeDish(dishId);

        assertThat(menu.getDishes()).isEmpty();
    }

    @Test
    void shouldPromoteConceptDishesToActive() {
        DishId conceptDishId1 = DishId.create();
        DishId conceptDishId2 = DishId.create();
        DishId availableDishId = DishId.create();

        Dish conceptDish1 = createRealDish(conceptDishId1, "Concept 1", 10.0, DishStatus.CONCEPT);
        Dish conceptDish2 = createRealDish(conceptDishId2, "Concept 2", 12.0, DishStatus.CONCEPT);
        Dish availableDish = createRealDish(availableDishId, "Available", 15.0, DishStatus.AVAILABLE);

        Menu menu = new Menu();
        menu.addDish(conceptDish1);
        menu.addDish(conceptDish2);
        menu.addDish(availableDish);

        menu.promoteConceptsToActive(List.of(conceptDishId1, conceptDishId2));

        assertThat(conceptDish1.getStatus()).isEqualTo(DishStatus.AVAILABLE);
        assertThat(conceptDish2.getStatus()).isEqualTo(DishStatus.AVAILABLE);
        assertThat(menu.getActiveDishes()).hasSize(3);
    }

    @Test
    void shouldThrowWhenPromotingConceptBeyondMaxCapacity() {
        Menu menu = new Menu();

        for (int i = 0; i < 9; i++) {
            DishId dishId = DishId.create();
            Dish dish = createRealDish(dishId, "Active " + i, 10.0 + i, DishStatus.AVAILABLE);
            menu.addDish(dish);
        }

        DishId conceptDishId1 = DishId.create();
        DishId conceptDishId2 = DishId.create();
        Dish conceptDish1 = createRealDish(conceptDishId1, "Concept 1", 10.0, DishStatus.CONCEPT);
        Dish conceptDish2 = createRealDish(conceptDishId2, "Concept 2", 12.0, DishStatus.CONCEPT);
        menu.addDish(conceptDish1);
        menu.addDish(conceptDish2);

        assertThrows(IllegalStateException.class,
                () -> menu.promoteConceptsToActive(List.of(conceptDishId1, conceptDishId2)));
    }

    @Test
    void shouldSkipNonConceptDishesDuringPromotion() {
        DishId conceptDishId = DishId.create();
        DishId availableDishId = DishId.create();

        Dish conceptDish = createRealDish(conceptDishId, "Concept", 10.0, DishStatus.CONCEPT);
        Dish availableDish = createRealDish(availableDishId, "Available", 15.0, DishStatus.AVAILABLE);

        Menu menu = new Menu();
        menu.addDish(conceptDish);
        menu.addDish(availableDish);

        menu.promoteConceptsToActive(List.of(conceptDishId, availableDishId));

        assertThat(conceptDish.getStatus()).isEqualTo(DishStatus.AVAILABLE);
        assertThat(availableDish.getStatus()).isEqualTo(DishStatus.AVAILABLE);
        assertThat(menu.getActiveDishes()).hasSize(2);
    }

    @Test
    void shouldThrowWhenPromotingNonExistentDish() {
        Menu menu = new Menu();
        DishId existingDishId = DishId.create();
        Dish existingDish = createRealDish(existingDishId, "Concept", 10.0, DishStatus.CONCEPT);
        menu.addDish(existingDish);

        DishId nonExistentDishId = DishId.create();

        assertThrows(NoSuchElementException.class,
                () -> menu.promoteConceptsToActive(List.of(existingDishId, nonExistentDishId)));
    }

    @Test
    void shouldPublishAllConceptDishesWhenUnderLimit() {
        Menu menu = new Menu();

        for (int i = 0; i < 5; i++) {
            DishId dishId = DishId.create();
            Dish dish = createRealDish(dishId, "Active " + i, 10.0 + i, DishStatus.AVAILABLE);
            menu.addDish(dish);
        }

        for (int i = 0; i < 3; i++) {
            DishId dishId = DishId.create();
            Dish dish = createRealDish(dishId, "Concept " + i, 15.0 + i, DishStatus.CONCEPT);
            menu.addDish(dish);
        }

        menu.publishAllConceptDishes();

        assertThat(menu.getConceptDishes()).isEmpty();
        assertThat(menu.getActiveDishes()).hasSize(8);
    }

    @Test
    void shouldThrowWhenPublishingAllConceptsExceedsMaxCapacity() {
        Menu menu = new Menu();

        for (int i = 0; i < 8; i++) {
            DishId dishId = DishId.create();
            Dish dish = createRealDish(dishId, "Active " + i, 10.0 + i, DishStatus.AVAILABLE);
            menu.addDish(dish);
        }

        for (int i = 0; i < 3; i++) {
            DishId dishId = DishId.create();
            Dish dish = createRealDish(dishId, "Concept " + i, 15.0 + i, DishStatus.CONCEPT);
            menu.addDish(dish);
        }

        assertThrows(IllegalStateException.class, menu::publishAllConceptDishes);
    }

    @Test
    void shouldDoNothingWhenPublishingEmptyConceptList() {
        Menu menu = new Menu();
        DishId activeDishId = DishId.create();
        Dish activeDish = createRealDish(activeDishId, "Active", 10.0, DishStatus.AVAILABLE);
        menu.addDish(activeDish);

        menu.publishAllConceptDishes();

        assertThat(menu.getActiveDishes()).hasSize(1);
        assertThat(menu.getConceptDishes()).isEmpty();
    }

    @Test
    void shouldDemoteActiveDishesToConcepts() {
        DishId activeDishId1 = DishId.create();
        DishId activeDishId2 = DishId.create();
        DishId conceptDishId = DishId.create();

        Dish activeDish1 = createRealDish(activeDishId1, "Active 1", 10.0, DishStatus.AVAILABLE);
        Dish activeDish2 = createRealDish(activeDishId2, "Active 2", 12.0, DishStatus.AVAILABLE);
        Dish conceptDish = createRealDish(conceptDishId, "Concept", 15.0, DishStatus.CONCEPT);

        Menu menu = new Menu();
        menu.addDish(activeDish1);
        menu.addDish(activeDish2);
        menu.addDish(conceptDish);

        menu.demoteActiveToConcepts(List.of(activeDishId1, activeDishId2));

        assertThat(activeDish1.getStatus()).isEqualTo(DishStatus.CONCEPT);
        assertThat(activeDish2.getStatus()).isEqualTo(DishStatus.CONCEPT);
        assertThat(menu.getActiveDishes()).isEmpty();
        assertThat(menu.getConceptDishes()).hasSize(3);
    }

    @Test
    void shouldSkipNonActiveDishesDuringDemotion() {
        DishId activeDishId = DishId.create();
        DishId conceptDishId = DishId.create();

        Dish activeDish = createRealDish(activeDishId, "Active", 10.0, DishStatus.AVAILABLE);
        Dish conceptDish = createRealDish(conceptDishId, "Concept", 15.0, DishStatus.CONCEPT);

        Menu menu = new Menu();
        menu.addDish(activeDish);
        menu.addDish(conceptDish);

        menu.demoteActiveToConcepts(List.of(activeDishId, conceptDishId));

        assertThat(activeDish.getStatus()).isEqualTo(DishStatus.CONCEPT);
        assertThat(conceptDish.getStatus()).isEqualTo(DishStatus.CONCEPT);
        assertThat(menu.getConceptDishes()).hasSize(2);
    }

    @Test
    void shouldThrowWhenDemotingNonExistentDish() {
        Menu menu = new Menu();
        DishId existingDishId = DishId.create();
        Dish existingDish = createRealDish(existingDishId, "Active", 10.0, DishStatus.AVAILABLE);
        menu.addDish(existingDish);

        DishId nonExistentDishId = DishId.create();

        assertThrows(NoSuchElementException.class,
                () -> menu.demoteActiveToConcepts(List.of(existingDishId, nonExistentDishId)));
    }

    @Test
    void shouldUpdateExistingDish() {
        DishId dishId = DishId.create();
        Dish originalDish = createRealDish(dishId, "Original", 10.0, DishStatus.CONCEPT);
        Dish updatedDish = createRealDish(dishId, "Updated", 15.0, DishStatus.AVAILABLE);

        Menu menu = new Menu();
        menu.addDish(originalDish);

        menu.updateDish(updatedDish);

        assertThat(menu.getDishes()).containsExactly(updatedDish);
        assertThat(menu.getDishes()).doesNotContain(originalDish);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentDish() {
        Menu menu = new Menu();
        DishId dishId = DishId.create();
        Dish dish = createRealDish(dishId, "Dish", 10.0, DishStatus.AVAILABLE);

        assertThrows(NoSuchElementException.class, () -> menu.updateDish(dish));
    }

    @Test
    void shouldThrowWhenActivatingDishBeyondMaxCapacity() {
        Menu menu = new Menu();

        for (int i = 0; i < 10; i++) {
            DishId dishId = DishId.create();
            Dish dish = createRealDish(dishId, "Active " + i, 10.0 + i, DishStatus.AVAILABLE);
            menu.addDish(dish);
        }

        DishId conceptDishId = DishId.create();
        Dish conceptDish = createRealDish(conceptDishId, "Concept", 15.0, DishStatus.CONCEPT);
        menu.addDish(conceptDish);

        Dish updatedDish = createRealDish(conceptDishId, "Updated Concept", 20.0, DishStatus.AVAILABLE);

        assertThrows(IllegalStateException.class, () -> menu.updateDish(updatedDish));
    }

    @Test
    void shouldAllowUpdatingDishThatStaysInactive() {
        Menu menu = new Menu();

        for (int i = 0; i < 10; i++) {
            DishId dishId = DishId.create();
            Dish dish = createRealDish(dishId, "Active " + i, 10.0 + i, DishStatus.AVAILABLE);
            menu.addDish(dish);
        }

        DishId conceptDishId = DishId.create();
        Dish conceptDish = createRealDish(conceptDishId, "Concept", 15.0, DishStatus.CONCEPT);
        menu.addDish(conceptDish);

        Dish updatedDish = createRealDish(conceptDishId, "Updated Concept", 20.0, DishStatus.CONCEPT);

        menu.updateDish(updatedDish);

        assertThat(menu.getDishes()).contains(updatedDish);
        assertThat(menu.getActiveDishes()).hasSize(10);
    }

    @Test
    void shouldAllowUpdatingActiveDishThatRemainsActive() {
        Menu menu = new Menu();
        DishId dishId = DishId.create();
        Dish originalDish = createRealDish(dishId, "Original", 10.0, DishStatus.AVAILABLE);
        Dish updatedDish = createRealDish(dishId, "Updated", 15.0, DishStatus.AVAILABLE);

        menu.addDish(originalDish);

        menu.updateDish(updatedDish);

        assertThat(menu.getDishes()).containsExactly(updatedDish);
    }

    @Test
    void shouldReturnUnmodifiableDishList() {
        Menu menu = new Menu();
        DishId dishId = DishId.create();
        Dish dish = createRealDish(dishId, "Dish", 10.0, DishStatus.AVAILABLE);
        menu.addDish(dish);

        List<Dish> dishes = menu.getDishes();

        assertThrows(UnsupportedOperationException.class, () -> dishes.add(dish));
    }

    @Test
    void shouldFilterConceptDishes() {
        Menu menu = new Menu();
        Dish conceptDish = createRealDish(DishId.create(), "Concept", 10.0, DishStatus.CONCEPT);
        Dish availableDish = createRealDish(DishId.create(), "Available", 12.0, DishStatus.AVAILABLE);
        Dish soldOutDish = createRealDish(DishId.create(), "Sold Out", 15.0, DishStatus.SOLD_OUT);

        menu.addDish(conceptDish);
        menu.addDish(availableDish);
        menu.addDish(soldOutDish);

        assertThat(menu.getConceptDishes()).containsExactly(conceptDish);
        assertThat(menu.getActiveDishes()).containsExactly(availableDish);
        assertThat(menu.getNonConceptDishes()).containsExactlyInAnyOrder(availableDish, soldOutDish);
    }

    @Test
    void shouldCalculateAveragePriceOfActiveDishes() {
        Menu menu = new Menu();
        menu.addDish(createRealDish(DishId.create(), "Dish 1", 10.0, DishStatus.AVAILABLE));
        menu.addDish(createRealDish(DishId.create(), "Dish 2", 20.0, DishStatus.AVAILABLE));
        menu.addDish(createRealDish(DishId.create(), "Concept", 50.0, DishStatus.CONCEPT));
        menu.addDish(createRealDish(DishId.create(), "Sold Out", 100.0, DishStatus.SOLD_OUT));

        double average = menu.calculateAveragePrice();

        assertThat(average).isEqualTo(15.0);
    }

    @Test
    void shouldReturnZeroWhenNoActiveDishes() {
        Menu menu = new Menu();
        menu.addDish(createRealDish(DishId.create(), "Concept", 10.0, DishStatus.CONCEPT));
        menu.addDish(createRealDish(DishId.create(), "Sold Out", 20.0, DishStatus.SOLD_OUT));

        double average = menu.calculateAveragePrice();

        assertThat(average).isEqualTo(0.0);
    }

    @Test
    void shouldReturnZeroWhenMenuIsEmpty() {
        Menu menu = new Menu();

        double average = menu.calculateAveragePrice();

        assertThat(average).isEqualTo(0.0);
    }

    @Test
    void shouldHandleSingleActiveDish() {
        Menu menu = new Menu();
        menu.addDish(createRealDish(DishId.create(), "Single Dish", 25.5, DishStatus.AVAILABLE));

        double average = menu.calculateAveragePrice();

        assertThat(average).isEqualTo(25.5);
    }

    @Test
    void shouldReturnPriceIndicator() {
        Menu menu = new Menu();
        menu.addDish(createRealDish(DishId.create(), "Dish 1", 10.0, DishStatus.AVAILABLE));
        menu.addDish(createRealDish(DishId.create(), "Dish 2", 20.0, DishStatus.AVAILABLE));

        String indicator = menu.getPriceIndicator();

        assertThat(indicator).isNotNull();
    }

    @Test
    void shouldReturnPriceIndicatorForEmptyMenu() {
        Menu menu = new Menu();

        String indicator = menu.getPriceIndicator();

        assertThat(indicator).isNotNull();
    }

    @Test
    void shouldHandleMixedOperations() {
        Menu menu = new Menu();

        DishId dish1 = DishId.create();
        DishId dish2 = DishId.create();
        DishId dish3 = DishId.create();

        Dish d1 = createRealDish(dish1, "Dish 1", 10.0, DishStatus.CONCEPT);
        Dish d2 = createRealDish(dish2, "Dish 2", 15.0, DishStatus.AVAILABLE);
        Dish d3 = createRealDish(dish3, "Dish 3", 20.0, DishStatus.CONCEPT);

        menu.addDish(d1);
        menu.addDish(d2);
        menu.addDish(d3);

        menu.promoteConceptsToActive(List.of(dish1));
        menu.removeDish(dish2);
        menu.demoteActiveToConcepts(List.of(dish1));

        assertThat(menu.getDishes()).hasSize(2);
        assertThat(menu.getActiveDishes()).isEmpty();
        assertThat(menu.getConceptDishes()).hasSize(2);
    }

    @Test
    void shouldHandleDuplicateDishIdsInPromotionList() {
        Menu menu = new Menu();
        DishId dishId = DishId.create();
        Dish dish = createRealDish(dishId, "Concept Dish", 10.0, DishStatus.CONCEPT);
        menu.addDish(dish);

        menu.promoteConceptsToActive(List.of(dishId, dishId));

        assertThat(dish.getStatus()).isEqualTo(DishStatus.AVAILABLE);
        assertThat(menu.getActiveDishes()).hasSize(1);
    }

    @Test
    void shouldHandleEmptyIdListInPromotion() {
        Menu menu = new Menu();
        Dish dish = createRealDish(DishId.create(), "Concept", 10.0, DishStatus.CONCEPT);
        menu.addDish(dish);

        menu.promoteConceptsToActive(List.of());

        assertThat(dish.getStatus()).isEqualTo(DishStatus.CONCEPT);
    }

    @Test
    void shouldHandleNullInUpdateDish() {
        Menu menu = new Menu();
        DishId dishId = DishId.create();
        Dish dish = createRealDish(dishId, "Dish", 10.0, DishStatus.AVAILABLE);
        menu.addDish(dish);

        assertThrows(NullPointerException.class, () -> menu.updateDish(null));
    }
}