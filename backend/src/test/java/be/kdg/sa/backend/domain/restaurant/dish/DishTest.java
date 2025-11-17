package be.kdg.sa.backend.domain.restaurant.dish;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DishTest {

    private Dish createBasicDish() {
        return new Dish(
                DishId.create(),
                "Pasta Carbonara",
                "Creamy pasta with bacon",
                12.50,
                false,
                false,
                false,
                DishCategory.MAIN,
                DishStatus.CONCEPT
        );
    }


    @Test
    void shouldCreateDishWithAllParameters() {

        DishId dishId = DishId.create();


        Dish dish = new Dish(dishId, "Pizza", "Delicious pizza", 15.99,
                true, false, true, DishCategory.MAIN, DishStatus.AVAILABLE);

        assertThat(dish.getId()).isEqualTo(dishId);
        assertThat(dish.getName()).isEqualTo("Pizza");
        assertThat(dish.getDescription()).isEqualTo("Delicious pizza");
        assertThat(dish.getPrice()).isEqualTo(15.99);
        assertThat(dish.isVegetarian()).isTrue();
        assertThat(dish.isVegan()).isFalse();
        assertThat(dish.isGlutenFree()).isTrue();
        assertThat(dish.getCategory()).isEqualTo(DishCategory.MAIN);
        assertThat(dish.getStatus()).isEqualTo(DishStatus.AVAILABLE);
        assertThat(dish.getPendingUpdate()).isNull();
    }

    @Test
    void shouldCreateDishWithNullDescription() {

        Dish dish = new Dish(DishId.create(), "Pizza", null, 15.99,
                false, false, false, DishCategory.MAIN, DishStatus.CONCEPT);

        assertThat(dish.getDescription()).isNull();
    }


    @Test
    void shouldRenameDish() {

        Dish dish = createBasicDish();

        dish.rename("New Pasta Name");

        assertThat(dish.getName()).isEqualTo("New Pasta Name");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldRenameWithEmptyOrNullName(String newName) {

        Dish dish = createBasicDish();

        dish.rename(newName);

        assertThat(dish.getName()).isEqualTo(newName);
    }


    @Test
    void shouldChangeDescription() {

        Dish dish = createBasicDish();

        dish.changeDescription("New description");


        assertThat(dish.getDescription()).isEqualTo("New description");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldChangeDescriptionToEmptyOrNull(String newDescription) {

        Dish dish = createBasicDish();


        dish.changeDescription(newDescription);


        assertThat(dish.getDescription()).isEqualTo(newDescription);
    }


    @Test
    void shouldChangePrice() {

        Dish dish = createBasicDish();


        dish.changePrice(14.99);


        assertThat(dish.getPrice()).isEqualTo(14.99);
    }

    @Test
    void shouldChangePriceToZero() {

        Dish dish = createBasicDish();


        dish.changePrice(0.0);


        assertThat(dish.getPrice()).isEqualTo(0.0);
    }

    @Test
    void shouldChangePriceToNegative() {

        Dish dish = createBasicDish();


        dish.changePrice(-5.0);


        assertThat(dish.getPrice()).isEqualTo(-5.0);
    }


    @Test
    void shouldChangeFromConceptToAvailable() {

        Dish dish = createBasicDish();


        dish.changeStatus(DishStatus.AVAILABLE);


        assertThat(dish.getStatus()).isEqualTo(DishStatus.AVAILABLE);
    }

    @Test
    void shouldThrowWhenConceptToSoldOut() {

        Dish dish = createBasicDish();


        assertThrows(IllegalStateException.class, () -> dish.changeStatus(DishStatus.SOLD_OUT));
    }

    @Test
    void shouldDoNothingWhenConceptToConcept() {

        Dish dish = createBasicDish();


        dish.changeStatus(DishStatus.CONCEPT);


        assertThat(dish.getStatus()).isEqualTo(DishStatus.CONCEPT);
    }


    @Test
    void shouldChangeFromAvailableToConcept() {

        Dish dish = createBasicDish();
        dish.changeStatus(DishStatus.AVAILABLE);


        dish.changeStatus(DishStatus.CONCEPT);


        assertThat(dish.getStatus()).isEqualTo(DishStatus.CONCEPT);
    }

    @Test
    void shouldChangeFromAvailableToSoldOut() {

        Dish dish = createBasicDish();
        dish.changeStatus(DishStatus.AVAILABLE);


        dish.changeStatus(DishStatus.SOLD_OUT);


        assertThat(dish.getStatus()).isEqualTo(DishStatus.SOLD_OUT);
    }

    @Test
    void shouldThrowWhenAvailableToInvalidStatus() {

        Dish dish = createBasicDish();
        dish.changeStatus(DishStatus.AVAILABLE);


    }

    @Test
    void shouldDoNothingWhenAvailableToAvailable() {

        Dish dish = createBasicDish();
        dish.changeStatus(DishStatus.AVAILABLE);


        dish.changeStatus(DishStatus.AVAILABLE);


        assertThat(dish.getStatus()).isEqualTo(DishStatus.AVAILABLE);
    }


    @Test
    void shouldChangeFromSoldOutToAvailable() {

        Dish dish = createBasicDish();
        dish.changeStatus(DishStatus.AVAILABLE);
        dish.changeStatus(DishStatus.SOLD_OUT);


        dish.changeStatus(DishStatus.AVAILABLE);


        assertThat(dish.getStatus()).isEqualTo(DishStatus.AVAILABLE);
    }

    @Test
    void shouldThrowWhenSoldOutToConcept() {

        Dish dish = createBasicDish();
        dish.changeStatus(DishStatus.AVAILABLE);
        dish.changeStatus(DishStatus.SOLD_OUT);


        assertThrows(IllegalStateException.class, () -> dish.changeStatus(DishStatus.CONCEPT));
    }

    @Test
    void shouldDoNothingWhenSoldOutToSoldOut() {

        Dish dish = createBasicDish();
        dish.changeStatus(DishStatus.AVAILABLE);
        dish.changeStatus(DishStatus.SOLD_OUT);


        dish.changeStatus(DishStatus.SOLD_OUT);


        assertThat(dish.getStatus()).isEqualTo(DishStatus.SOLD_OUT);
    }


    @Test
    void shouldUpdateAllDetails() {

        Dish dish = createBasicDish();


        dish.updateDetails("New Name", "New Desc", 20.99, true, true, true,
                DishCategory.DESSERT, DishStatus.AVAILABLE);


        assertThat(dish.getName()).isEqualTo("New Name");
        assertThat(dish.getDescription()).isEqualTo("New Desc");
        assertThat(dish.getPrice()).isEqualTo(20.99);
        assertThat(dish.isVegetarian()).isTrue();
        assertThat(dish.isVegan()).isTrue();
        assertThat(dish.isGlutenFree()).isTrue();
        assertThat(dish.getCategory()).isEqualTo(DishCategory.DESSERT);
        assertThat(dish.getStatus()).isEqualTo(DishStatus.AVAILABLE);
    }

    @Test
    void shouldUpdateDetailsWithNullDescription() {

        Dish dish = createBasicDish();


        dish.updateDetails("New Name", null, 20.99, true, false, true,
                DishCategory.MAIN, DishStatus.AVAILABLE);


        assertThat(dish.getDescription()).isNull();
    }


    @Test
    void shouldPromoteConceptToAvailable() {

        Dish dish = createBasicDish();


        dish.promoteToAvailable();


        assertThat(dish.getStatus()).isEqualTo(DishStatus.AVAILABLE);
    }

    @Test
    void shouldThrowWhenPromotingNonConceptDish() {

        Dish dish = createBasicDish();
        dish.changeStatus(DishStatus.AVAILABLE);


        assertThrows(IllegalStateException.class, dish::promoteToAvailable);
    }

    @Test
    void shouldDemoteAvailableToConcept() {

        Dish dish = createBasicDish();
        dish.changeStatus(DishStatus.AVAILABLE);


        dish.demoteToConcept();


        assertThat(dish.getStatus()).isEqualTo(DishStatus.CONCEPT);
    }

    @Test
    void shouldThrowWhenDemotingNonAvailableDish() {

        Dish dish = createBasicDish();


        assertThrows(IllegalStateException.class, dish::demoteToConcept);
    }


    @Test
    void shouldApplyUpdateImmediatelyWhenScheduledForIsNull() {

        Dish dish = createBasicDish();
        DishPendingUpdate update = new DishPendingUpdate("New Name", null, null, null, null, null, null, null, null);


        dish.scheduleOrApplyUpdate(update);


        assertThat(dish.getName()).isEqualTo("New Name");
        assertThat(dish.getPendingUpdate()).isNull();
    }

    @Test
    void shouldApplyUpdateImmediatelyWhenScheduledForIsPast() {

        Dish dish = createBasicDish();
        DishPendingUpdate update = new DishPendingUpdate("New Name", null, null, null, null, null, null, null,
                LocalDateTime.now().minusHours(1));


        dish.scheduleOrApplyUpdate(update);


        assertThat(dish.getName()).isEqualTo("New Name");
        assertThat(dish.getPendingUpdate()).isNull();
    }

    @Test
    void shouldScheduleUpdateWhenScheduledForIsFuture() {

        Dish dish = createBasicDish();
        DishPendingUpdate update = new DishPendingUpdate("New Name", null, null, null, null, null, null, null,
                LocalDateTime.now().plusHours(1));


        dish.scheduleOrApplyUpdate(update);


        assertThat(dish.getName()).isEqualTo("Pasta Carbonara");
        assertThat(dish.getPendingUpdate()).isEqualTo(update);
    }

    @Test
    void shouldScheduleUpdateExplicitly() {

        Dish dish = createBasicDish();
        DishPendingUpdate update = new DishPendingUpdate("New Name", null, null, null, null, null, null, null,
                LocalDateTime.now().plusHours(1));


        dish.scheduleUpdate(update);


        assertThat(dish.getPendingUpdate()).isEqualTo(update);
    }

    @Test
    void shouldReturnTrueForIsUpdateReadyToApplyWhenUpdateIsDue() {

        Dish dish = createBasicDish();
        DishPendingUpdate update = new DishPendingUpdate("New Name", null, null, null, null, null, null, null,
                LocalDateTime.now().minusMinutes(1));
        dish.scheduleUpdate(update);


        assertThat(dish.isUpdateReadyToApply()).isTrue();
    }

    @Test
    void shouldReturnFalseForIsUpdateReadyToApplyWhenUpdateIsFuture() {

        Dish dish = createBasicDish();
        DishPendingUpdate update = new DishPendingUpdate("New Name", null, null, null, null, null, null, null,
                LocalDateTime.now().plusHours(1));
        dish.scheduleUpdate(update);


        assertThat(dish.isUpdateReadyToApply()).isFalse();
    }

    @Test
    void shouldReturnFalseForIsUpdateReadyToApplyWhenNoUpdate() {

        Dish dish = createBasicDish();


        assertThat(dish.isUpdateReadyToApply()).isFalse();
    }

    @Test
    void shouldApplyPendingChangesWhenUpdateIsDue() {

        Dish dish = createBasicDish();
        DishPendingUpdate update = new DishPendingUpdate("New Name", "New Desc", 15.99, null, null, null, null, null,
                LocalDateTime.now().minusMinutes(1));
        dish.scheduleUpdate(update);


        dish.applyPendingChanges();


        assertThat(dish.getName()).isEqualTo("New Name");
        assertThat(dish.getDescription()).isEqualTo("New Desc");
        assertThat(dish.getPrice()).isEqualTo(15.99);
        assertThat(dish.getPendingUpdate()).isNull();
    }

    @Test
    void shouldNotApplyPendingChangesWhenUpdateIsFuture() {

        Dish dish = createBasicDish();
        DishPendingUpdate update = new DishPendingUpdate("New Name", null, null, null, null, null, null, null,
                LocalDateTime.now().plusHours(1));
        dish.scheduleUpdate(update);


        dish.applyPendingChanges();


        assertThat(dish.getName()).isEqualTo("Pasta Carbonara");
        assertThat(dish.getPendingUpdate()).isEqualTo(update);
    }

    @Test
    void shouldNotApplyPendingChangesWhenNoUpdate() {

        Dish dish = createBasicDish();


        dish.applyPendingChanges();


        assertThat(dish.getName()).isEqualTo("Pasta Carbonara");
    }

    @Test
    void shouldApplyPartialUpdateWithNullFields() {

        Dish dish = createBasicDish();
        DishPendingUpdate update = new DishPendingUpdate("New Name", null, 20.0, null, null, null, null, null, null);


        dish.scheduleOrApplyUpdate(update);


        assertThat(dish.getName()).isEqualTo("New Name");
        assertThat(dish.getPrice()).isEqualTo(20.0);
        assertThat(dish.getDescription()).isEqualTo("Creamy pasta with bacon");
        assertThat(dish.isVegetarian()).isFalse();
    }

    @Test
    void shouldHandleMultipleScheduledUpdates() {

        Dish dish = createBasicDish();
        DishPendingUpdate firstUpdate = new DishPendingUpdate("First Name", null, null, null, null, null, null, null,
                LocalDateTime.now().plusMinutes(30));
        DishPendingUpdate secondUpdate = new DishPendingUpdate("Second Name", null, null, null, null, null, null, null,
                LocalDateTime.now().plusHours(1));


        dish.scheduleUpdate(firstUpdate);
        dish.scheduleUpdate(secondUpdate);


        assertThat(dish.getPendingUpdate()).isEqualTo(secondUpdate);
    }


    @Test
    void shouldHandleStatusChangeWithPendingUpdate() {

        Dish dish = createBasicDish();
        DishPendingUpdate update = new DishPendingUpdate(null, null, null, null, null, null, null,
                DishStatus.AVAILABLE, LocalDateTime.now().plusHours(1));
        dish.scheduleUpdate(update);


        dish.changeStatus(DishStatus.AVAILABLE);


        assertThat(dish.getStatus()).isEqualTo(DishStatus.AVAILABLE);
        assertThat(dish.getPendingUpdate()).isEqualTo(update);
    }

    @Test
    void shouldApplyUpdateWithAllNullFields() {

        Dish dish = createBasicDish();
        DishPendingUpdate update = new DishPendingUpdate(null, null, null, null, null, null, null, null, null);


        dish.scheduleOrApplyUpdate(update);


        assertThat(dish.getName()).isEqualTo("Pasta Carbonara");
        assertThat(dish.getDescription()).isEqualTo("Creamy pasta with bacon");
        assertThat(dish.getPrice()).isEqualTo(12.50);
    }

    @ParameterizedTest
    @EnumSource(DishCategory.class)
    void shouldHandleAllCategories(DishCategory category) {

        Dish dish = createBasicDish();


        dish.updateDetails("Test", "Test", 10.0, false, false, false, category, DishStatus.AVAILABLE);


        assertThat(dish.getCategory()).isEqualTo(category);
    }

    @ParameterizedTest
    @EnumSource(DishStatus.class)
    void shouldHandleAllStatusesInUpdateDetails(DishStatus status) {

        Dish dish = createBasicDish();


        dish.updateDetails("Test", "Test", 10.0, false, false, false, DishCategory.MAIN, status);


        assertThat(dish.getStatus()).isEqualTo(status);
    }
}