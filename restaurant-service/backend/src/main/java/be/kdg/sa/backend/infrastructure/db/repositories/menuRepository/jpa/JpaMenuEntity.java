package be.kdg.sa.backend.infrastructure.db.repositories.menuRepository.jpa;

import be.kdg.sa.backend.domain.restaurant.Menu;
import be.kdg.sa.backend.domain.restaurant.dish.Dish;
import be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository.jpa.JpaRestaurantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Entity
@Table(name = "menus")
public class JpaMenuEntity {

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<JpaDishEntity> dishes = new LinkedHashSet<>();

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_menu_restaurant"))
    @Setter
    private JpaRestaurantEntity restaurant;

    protected JpaMenuEntity() {
    }

    public JpaMenuEntity(UUID id) {
        this.id = id;
    }

    public static JpaMenuEntity fromDomain(Menu menu, JpaRestaurantEntity restaurant) {
        JpaMenuEntity entity = new JpaMenuEntity(UUID.randomUUID());
        entity.setRestaurant(restaurant);

        menu.getDishes().forEach(dish -> entity.dishes.add(JpaDishEntity.fromDomain(dish, entity)));

        return entity;
    }

    public Menu toDomain() {
        return Menu.reconstruct(dishes.stream().map(JpaDishEntity::toDomain).toList());
    }

    public void updateFromDomain(Menu menuDomain) {
        if (menuDomain == null) return;

        Map<UUID, JpaDishEntity> current = dishes.stream()
                .collect(Collectors.toMap(JpaDishEntity::getId, Function.identity()));

        for (Dish d : menuDomain.getDishes()) {
            UUID did = d.getId().id();
            JpaDishEntity existing = current.remove(did);
            if (existing != null) {
                existing.updateFromDomain(d);
            } else {
                dishes.add(JpaDishEntity.fromDomain(d, this));
            }
        }

        // remove stale ones
        for (JpaDishEntity r : current.values()) {
            dishes.remove(r);
        }
    }
}