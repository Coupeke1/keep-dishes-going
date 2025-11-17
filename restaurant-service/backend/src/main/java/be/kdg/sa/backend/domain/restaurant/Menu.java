package be.kdg.sa.backend.domain.restaurant;

import be.kdg.sa.backend.domain.restaurant.dish.Dish;
import be.kdg.sa.backend.domain.restaurant.dish.DishId;
import be.kdg.sa.backend.domain.restaurant.dish.DishStatus;
import org.jmolecules.ddd.annotation.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Entity
public class Menu {
    private final List<Dish> dishes = new ArrayList<>();
    private static final int MAX_ACTIVE_DISHES = 10;

    public Menu() {
    }

    private Menu(List<Dish> dishes) {
        this.dishes.addAll(dishes);
    }

    public static Menu reconstruct(List<Dish> dishes) {
        return new Menu(dishes);
    }


    public void addDish(Dish dish) {
        if (dish.getStatus() == DishStatus.AVAILABLE && getActiveDishes().size() >= MAX_ACTIVE_DISHES) {
            throw new IllegalStateException("Menu can have at most " + MAX_ACTIVE_DISHES + " active dishes");
        }
        this.dishes.add(dish);
    }


    public void removeDish(DishId dishId) {
        if (!dishes.removeIf(d -> d.getId().equals(dishId))) {
            throw new NoSuchElementException("Dish not found: " + dishId);
        }
    }

    public void promoteConceptsToActive(List<DishId> dishIds) {
        for (DishId id : dishIds) {
            Dish dish = findDishOrThrow(id);
            if (dish.getStatus() == DishStatus.CONCEPT) {
                if (getActiveDishes().size() >= MAX_ACTIVE_DISHES) {
                    throw new IllegalStateException("Cannot promote more dishes: max active dishes reached");
                }
                dish.promoteToAvailable();
            }
        }
    }

    public void publishAllConceptDishes() {
        List<Dish> conceptDishes = getConceptDishes();
        if (getActiveDishes().size() + conceptDishes.size() > MAX_ACTIVE_DISHES) {
            throw new IllegalStateException("Cannot publish all concept dishes. Maximum of " + MAX_ACTIVE_DISHES + " available dishes allowed.");
        }
        conceptDishes.forEach(Dish::promoteToAvailable);
    }

    public void demoteActiveToConcepts(List<DishId> dishIds) {
        for (DishId id : dishIds) {
            Dish dish = findDishOrThrow(id);
            if (dish.getStatus() == DishStatus.AVAILABLE) {
                dish.demoteToConcept();
            }
        }
    }


    public void updateDish(Dish updatedDish) {
        Dish existing = findDishOrThrow(updatedDish.getId());
        boolean becomingActive = updatedDish.getStatus() == DishStatus.AVAILABLE && existing.getStatus() != DishStatus.AVAILABLE;

        if (becomingActive && getActiveDishes().size() >= MAX_ACTIVE_DISHES) {
            throw new IllegalStateException("Cannot activate dish: max active dishes reached");
        }

        dishes.remove(existing);
        dishes.add(updatedDish);
    }

    public List<Dish> getDishes() {
        return Collections.unmodifiableList(dishes);
    }

    public List<Dish> getConceptDishes() {
        return dishes.stream()
                .filter(d -> d.getStatus() == DishStatus.CONCEPT)
                .toList();
    }

    public List<Dish> getActiveDishes() {
        return dishes.stream()
                .filter(d -> d.getStatus() == DishStatus.AVAILABLE)
                .toList();
    }

    public List<Dish> getNonConceptDishes() {
        return dishes.stream()
                .filter(d -> d.getStatus() != DishStatus.CONCEPT)
                .toList();
    }

    private Dish findDishOrThrow(DishId id) {
        return dishes.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Dish not found: " + id));
    }

    public double calculateAveragePrice() {
        return dishes.stream()
                .filter(d -> d.getStatus() == DishStatus.AVAILABLE)
                .mapToDouble(Dish::getPrice)
                .average()
                .orElse(0.0);
    }

    public String getPriceIndicator() {
        double averagePrice = calculateAveragePrice();
        return PriceTier.from(averagePrice).symbol();
    }
}
