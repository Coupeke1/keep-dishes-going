package be.kdg.sa.backend.domain.restaurant;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.owner.OwnerId;
import be.kdg.sa.backend.domain.restaurant.dish.Dish;
import be.kdg.sa.backend.domain.restaurant.dish.DishCategory;
import be.kdg.sa.backend.domain.restaurant.dish.DishId;
import be.kdg.sa.backend.domain.restaurant.dish.DishStatus;
import be.kdg.sa.backend.domain.restaurant.openingHours.OpeningHours;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@AggregateRoot
public class Restaurant {
    @Identity
    private final RestaurantId id;

    private final Address address;
    private final String phoneNumber;
    private final String email;
    private final Menu menu;

    private String name;
    private String cuisineType;
    private String logoUrl;
    private OpeningHours openingHours;

    private Boolean manualOverrideOpen;
    private LocalDateTime overrideUntil;

    private final OwnerId ownerId;

    private Restaurant(RestaurantId id, String name, Address address, String phoneNumber, String email,
                       String cuisineType, Menu menu, OwnerId ownerId) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.cuisineType = cuisineType;
        this.menu = (menu != null) ? menu : new Menu();
        this.ownerId = ownerId;
    }

    public static Restaurant create(String name, Address address, String phoneNumber, String email,
                                    String cuisineType, OwnerId ownerId) {
        Objects.requireNonNull(ownerId, "Restaurant must be created by an owner");
        return new Restaurant(RestaurantId.create(), name, address, phoneNumber, email, cuisineType, null, ownerId);
    }

    public static Restaurant reconstruct(RestaurantId id, String name, Address address, String phoneNumber,
                                         String email, String cuisineType, Menu menu, OwnerId ownerId) {
        return new Restaurant(id, name, address, phoneNumber, email, cuisineType, menu, ownerId);
    }


    public Dish addDish(String name, String description, double price, boolean vegetarian, boolean vegan,
                        boolean glutenFree, DishCategory category, DishStatus status) {
        Dish dish = new Dish(DishId.create(), name, description, price, vegetarian, vegan, glutenFree, category, status);
        menu.addDish(dish);
        return dish;
    }

    public Dish changeDish(DishId dishId, String name, String description, double price,
                           boolean vegetarian, boolean vegan, boolean glutenFree,
                           DishCategory category, DishStatus newStatus) {
        Dish existingDish = getDish(dishId);
        existingDish.updateDetails(name, description, price, vegetarian, vegan, glutenFree, category, newStatus);
        menu.updateDish(existingDish);
        return existingDish;
    }

    public void rename(String updatedRestaurantName) {
        this.name = updatedRestaurantName;
    }

    public void changeCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public void updateLogo(String url) {
        this.logoUrl = url;
    }

    public Dish changeDishStatus(DishId dishId, DishStatus newStatus) {
        Dish dish = getDish(dishId);
        dish.changeStatus(newStatus);
        menu.updateDish(dish);
        return dish;
    }

    public void removeDish(DishId dishId) {
        menu.removeDish(dishId);
    }

    private Dish getDish(DishId dishId) {
        return menu.getDishes().stream()
                .filter(d -> d.getId().equals(dishId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dish not found: " + dishId));
    }

    public boolean isOwnedBy(OwnerId ownerId) {
        return this.ownerId.equals(ownerId);
    }

    public boolean isCurrentlyOpen() {
        if (openingHours == null && manualOverrideOpen == null) return false;
        if (manualOverrideOpen != null) {
            if (overrideUntil == null || overrideUntil.isAfter(LocalDateTime.now())) {
                return manualOverrideOpen;
            } else {
                manualOverrideOpen = null;
            }
        }
        return openingHours != null && openingHours.isOpenAt(
                LocalDateTime.now().getDayOfWeek(),
                LocalDateTime.now().toLocalTime()
        );
    }

    public void overrideOpeningManually(boolean open, LocalDateTime until) {
        this.manualOverrideOpen = open;
        this.overrideUntil = until;
    }

    public void updateOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public void publishAllConceptDishes() {
        menu.publishAllConceptDishes();
    }

    public List<Dish> getNonConceptDishes() {
        return menu.getNonConceptDishes();
    }

    public double getAveragePrice() {
        return menu.calculateAveragePrice();
    }

    public String getPriceIndicator() {
        return menu.getPriceIndicator();
    }

}
