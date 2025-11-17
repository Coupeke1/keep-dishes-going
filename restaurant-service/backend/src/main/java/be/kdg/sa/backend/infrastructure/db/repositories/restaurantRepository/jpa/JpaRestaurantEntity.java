package be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository.jpa;

import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.owner.OwnerId;
import be.kdg.sa.backend.domain.restaurant.Menu;
import be.kdg.sa.backend.domain.restaurant.Restaurant;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import be.kdg.sa.backend.domain.restaurant.openingHours.OpeningHours;
import be.kdg.sa.backend.infrastructure.db.converters.OpeningHoursJsonConverter;
import be.kdg.sa.backend.infrastructure.db.repositories.JpaAddressEntity;
import be.kdg.sa.backend.infrastructure.db.repositories.menuRepository.jpa.JpaMenuEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "restaurants")
public class JpaRestaurantEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_restaurant_address"))
    private JpaAddressEntity address;

    @Column(nullable = false)
    private String name;

    private String cuisineType;
    private String phoneNumber;
    private String email;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "manual_override_open")
    private Boolean manualOverrideOpen;

    @Column(name = "override_until")
    private LocalDateTime overrideUntil;

    @Column(name = "opening_hours", columnDefinition = "jsonb")
    @Convert(converter = OpeningHoursJsonConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    private OpeningHours openingHours;

    @OneToOne(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private JpaMenuEntity menu;

    protected JpaRestaurantEntity() {
    }

    public JpaRestaurantEntity(UUID id, String name, String cuisineType, JpaAddressEntity address,
                               String phoneNumber, String email, OpeningHours openingHours, UUID ownerId) {
        this.id = id;
        this.name = name;
        this.cuisineType = cuisineType;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.manualOverrideOpen = false;
        this.overrideUntil = null;
        this.openingHours = openingHours;
        this.ownerId = ownerId;
    }

    public static JpaRestaurantEntity fromDomain(Restaurant restaurant) {
        if (restaurant == null) return null;
        UUID id = restaurant.getId() != null ? restaurant.getId().id() : null;
        JpaAddressEntity addressEntity = JpaAddressEntity.fromDomain(restaurant.getAddress());

        JpaRestaurantEntity entity = new JpaRestaurantEntity(
                id,
                restaurant.getName(),
                restaurant.getCuisineType(),
                addressEntity,
                restaurant.getPhoneNumber(),
                restaurant.getEmail(),
                restaurant.getOpeningHours(),
                restaurant.getOwnerId().id()
        );
        entity.manualOverrideOpen = restaurant.getManualOverrideOpen();
        entity.overrideUntil = restaurant.getOverrideUntil();
        return entity;
    }

    public Restaurant toDomain() {
        Address addressDomain = address != null ? address.toDomain() : null;
        Menu menuDomain = menu != null ? menu.toDomain() : new Menu();

        Restaurant restaurant = Restaurant.reconstruct(
                new RestaurantId(id),
                name,
                addressDomain,
                phoneNumber,
                email,
                cuisineType,
                menuDomain,
                new OwnerId(ownerId)
        );

        restaurant.updateOpeningHours(openingHours);
        if (manualOverrideOpen != null) {
            restaurant.overrideOpeningManually(manualOverrideOpen, overrideUntil);
        }

        return restaurant;
    }

    public void updateFromDomain(Restaurant restaurant) {
        if (restaurant == null) return;

        this.id = restaurant.getId().id();
        this.name = restaurant.getName();
        this.cuisineType = restaurant.getCuisineType();
        this.phoneNumber = restaurant.getPhoneNumber();
        this.email = restaurant.getEmail();
        this.ownerId = restaurant.getOwnerId().id();
        this.openingHours = restaurant.getOpeningHours();
        this.manualOverrideOpen = restaurant.getManualOverrideOpen();
        this.overrideUntil = restaurant.getOverrideUntil();

        if (this.address != null && restaurant.getAddress() != null) {
            this.address.updateFromDomain(restaurant.getAddress());
        } else if (restaurant.getAddress() != null) {
            this.address = JpaAddressEntity.fromDomain(restaurant.getAddress());
        } else {
            this.address = null;
        }

        if (restaurant.getMenu() != null) {
            if (this.menu == null) {
                this.menu = JpaMenuEntity.fromDomain(restaurant.getMenu(), this);
            } else {
                this.menu.updateFromDomain(restaurant.getMenu());
            }
        } else {
            this.menu = null;
        }
    }
}
