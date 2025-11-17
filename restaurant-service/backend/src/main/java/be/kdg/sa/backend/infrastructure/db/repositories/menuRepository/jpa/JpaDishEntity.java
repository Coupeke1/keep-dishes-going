package be.kdg.sa.backend.infrastructure.db.repositories.menuRepository.jpa;

import be.kdg.sa.backend.domain.restaurant.dish.*;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "dishes")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JpaDishEntity {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private boolean vegetarian;

    @Column(nullable = false)
    private boolean vegan;

    @Column(name = "gluten_free", nullable = false)
    private boolean glutenFree;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private DishCategory category;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private DishStatus status;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = JpaMenuEntity.class)
    @JoinColumn(name = "menu_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_dish_menu"))

    private JpaMenuEntity menu;

    @Column(name = "pending_name")
    private String pendingName;

    @Column(name = "pending_description", columnDefinition = "TEXT")
    private String pendingDescription;

    @Column(name = "pending_price")
    private Double pendingPrice;

    @Column(name = "pending_vegetarian")
    private Boolean pendingVegetarian;

    @Column(name = "pending_vegan")
    private Boolean pendingVegan;

    @Column(name = "pending_gluten_free")
    private Boolean pendingGlutenFree;

    @Enumerated(EnumType.STRING)
    @Column(name = "pending_category", columnDefinition = "varchar(50)")
    private DishCategory pendingCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "pending_status", columnDefinition = "varchar(20)")
    private DishStatus pendingStatus;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;

    protected JpaDishEntity() {
    }

    public JpaDishEntity(UUID id, String name, String description, double price,
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

    public static JpaDishEntity fromDomain(Dish dish, JpaMenuEntity menu) {
        JpaDishEntity entity = new JpaDishEntity(
                dish.getId().id(),
                dish.getName(),
                dish.getDescription(),
                dish.getPrice(),
                dish.isVegetarian(),
                dish.isVegan(),
                dish.isGlutenFree(),
                dish.getCategory(),
                dish.getStatus()
        );
        entity.menu = menu;

        if (dish.getPendingUpdate() != null) {
            var pending = dish.getPendingUpdate();
            entity.pendingName = pending.name();
            entity.pendingDescription = pending.description();
            entity.pendingPrice = pending.price();
            entity.pendingVegetarian = pending.vegetarian();
            entity.pendingVegan = pending.vegan();
            entity.pendingGlutenFree = pending.glutenFree();
            entity.pendingCategory = pending.category();
            entity.pendingStatus = pending.status();
            entity.scheduledFor = pending.scheduledFor();
        }
        return entity;
    }

    public Dish toDomain() {
        Dish dish = new Dish(
                new DishId(id),
                name,
                description,
                price,
                vegetarian,
                vegan,
                glutenFree,
                category,
                status
        );

        if (scheduledFor != null) {
            dish.scheduleUpdate(new DishPendingUpdate(
                    pendingName, pendingDescription, pendingPrice,
                    pendingVegetarian, pendingVegan, pendingGlutenFree,
                    pendingCategory, pendingStatus, scheduledFor
            ));
        }

        return dish;
    }

    public void updateFromDomain(Dish dish) {
        if (dish == null) return;

        this.name = dish.getName();
        this.description = dish.getDescription();
        this.price = dish.getPrice();
        this.vegetarian = dish.isVegetarian();
        this.vegan = dish.isVegan();
        this.glutenFree = dish.isGlutenFree();
        this.category = dish.getCategory();
        this.status = dish.getStatus();

        if (dish.getPendingUpdate() != null) {
            var pending = dish.getPendingUpdate();
            this.pendingName = pending.name();
            this.pendingDescription = pending.description();
            this.pendingPrice = pending.price();
            this.pendingVegetarian = pending.vegetarian();
            this.pendingVegan = pending.vegan();
            this.pendingGlutenFree = pending.glutenFree();
            this.pendingCategory = pending.category();
            this.pendingStatus = pending.status();
            this.scheduledFor = pending.scheduledFor();
        } else {
            this.pendingName = null;
            this.pendingDescription = null;
            this.pendingPrice = null;
            this.pendingVegetarian = null;
            this.pendingVegan = null;
            this.pendingGlutenFree = null;
            this.pendingCategory = null;
            this.pendingStatus = null;
            this.scheduledFor = null;
        }
    }
}
