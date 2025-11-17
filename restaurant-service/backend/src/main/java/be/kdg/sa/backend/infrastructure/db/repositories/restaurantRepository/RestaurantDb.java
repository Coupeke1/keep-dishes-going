package be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository;

import be.kdg.sa.backend.domain.NotFoundException;
import be.kdg.sa.backend.domain.owner.OwnerId;
import be.kdg.sa.backend.domain.restaurant.Restaurant;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import be.kdg.sa.backend.domain.restaurant.dish.Dish;
import be.kdg.sa.backend.infrastructure.db.repositories.menuRepository.jpa.JpaDishEntity;
import be.kdg.sa.backend.infrastructure.db.repositories.menuRepository.jpa.JpaMenuEntity;
import be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository.jpa.JpaRestaurantEntity;
import be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository.jpa.JpaRestaurantRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class RestaurantDb implements RestaurantRepository {

    private final JpaRestaurantRepository jpaRestaurantRepository;

    public RestaurantDb(JpaRestaurantRepository jpaRestaurantRepository) {
        this.jpaRestaurantRepository = jpaRestaurantRepository;
    }

    @Override
    public Optional<Restaurant> findById(RestaurantId id) {
        return jpaRestaurantRepository.findById(id.id())
                .map(JpaRestaurantEntity::toDomain);
    }

    @Override
    public Optional<Restaurant> findByOwnerId(UUID ownerId) {
        return jpaRestaurantRepository.findByOwnerId(ownerId)
                .map(JpaRestaurantEntity::toDomain);
    }

    @Override
    public void save(Restaurant restaurant) {
        JpaRestaurantEntity entity = jpaRestaurantRepository.findById(restaurant.getId().id())
                .orElseGet(() -> JpaRestaurantEntity.fromDomain(restaurant));
        entity.updateFromDomain(restaurant);
        jpaRestaurantRepository.save(entity);
    }

    @Override
    public Collection<Restaurant> findAll() {
        return jpaRestaurantRepository.findAll().stream()
                .map(JpaRestaurantEntity::toDomain)
                .toList();
    }

    @Override
    public Collection<Dish> findAllDishesByRestaurantId(RestaurantId id) {
        return jpaRestaurantRepository.findById(id.id())
                .map(JpaRestaurantEntity::getMenu)
                .map(JpaMenuEntity::getDishes)
                .map(dishes -> dishes.stream()
                        .map(JpaDishEntity::toDomain)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new NotFoundException("restaurant not found: " + id));
    }

    @Override
    public boolean existsByOwnerId(OwnerId ownerId) {
        return jpaRestaurantRepository.existsByOwnerId(ownerId.id());
    }
}
