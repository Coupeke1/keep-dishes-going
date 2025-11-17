package be.kdg.sa.backend.domain.restaurant.dish;

import lombok.Getter;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;

import java.time.LocalDateTime;

@Getter
@Entity
public class Dish {
    @Identity
    private final DishId id;
    private String name;
    private String description;
    private double price;
    private boolean vegetarian;
    private boolean vegan;
    private boolean glutenFree;
    private DishCategory category;
    private DishStatus status;
    private DishPendingUpdate pendingUpdate;

    public Dish(DishId id, String name, String description, double price,
                boolean vegetarian, boolean vegan, boolean glutenFree,
                DishCategory category, DishStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.vegetarian = vegetarian;
        this.vegan = vegan;
        this.glutenFree = glutenFree;
        this.category = category;
        this.status = status;
    }

    public void rename(String newName) {
        this.name = newName;
    }

    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    public void changePrice(double newPrice) {
        this.price = newPrice;
    }

    public void changeStatus(DishStatus newStatus) {
        if (this.status == newStatus) return;

        switch (this.status) {
            case CONCEPT -> {
                if (newStatus != DishStatus.AVAILABLE) {
                    throw new IllegalStateException("Concept dishes can only be promoted to available");
                }
            }
            case AVAILABLE -> {
                if (newStatus == DishStatus.CONCEPT) {
                    this.status = DishStatus.CONCEPT;
                    return;
                }
                if (newStatus == DishStatus.SOLD_OUT) {
                    this.status = DishStatus.SOLD_OUT;
                    return;
                }
                throw new IllegalStateException("Available dishes can only be demoted to concept or sold out");
            }
            case SOLD_OUT -> {
                if (newStatus == DishStatus.AVAILABLE) {
                    this.status = DishStatus.AVAILABLE;
                    return;
                }
                throw new IllegalStateException("Sold out dishes can only be restocked to available");
            }
        }
        this.status = newStatus;
    }

    public void updateDetails(String name, String description, double price, boolean vegetarian, boolean vegan, boolean glutenFree, DishCategory category, DishStatus newStatus) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.vegetarian = vegetarian;
        this.vegan = vegan;
        this.glutenFree = glutenFree;
        this.category = category;
        this.status = newStatus;
    }

    public void promoteToAvailable() {
        if (this.status != DishStatus.CONCEPT) {
            throw new IllegalStateException("Only concept dishes can be promoted to available");
        }
        this.status = DishStatus.AVAILABLE;
    }

    public void demoteToConcept() {
        if (this.status != DishStatus.AVAILABLE) {
            throw new IllegalStateException("Only available dishes can be demoted to concept");
        }
        this.status = DishStatus.CONCEPT;
    }

    public void scheduleOrApplyUpdate(DishPendingUpdate update) {
        if (update.scheduledFor() == null || !update.scheduledFor().isAfter(LocalDateTime.now())) {
            applyUpdate(update);
        } else {
            this.pendingUpdate = update;
        }
    }

    public boolean isUpdateReadyToApply() {
        return pendingUpdate != null && !LocalDateTime.now().isBefore(pendingUpdate.scheduledFor());
    }

    public boolean hasPendingChanges() {
        return pendingUpdate != null;
    }

    public void applyPendingChanges() {
        if (!isUpdateReadyToApply()) return;
        applyUpdate(pendingUpdate);
        pendingUpdate = null;
    }

    private void applyUpdate(DishPendingUpdate update) {
        if (update.name() != null) this.name = update.name();
        if (update.description() != null) this.description = update.description();
        if (update.price() != null) this.price = update.price();
        if (update.vegetarian() != null) this.vegetarian = update.vegetarian();
        if (update.vegan() != null) this.vegan = update.vegan();
        if (update.glutenFree() != null) this.glutenFree = update.glutenFree();
        if (update.category() != null) this.category = update.category();
        if (update.status() != null) this.status = update.status();
    }

    public void scheduleUpdate(DishPendingUpdate update) {
        this.pendingUpdate = update;
    }
}