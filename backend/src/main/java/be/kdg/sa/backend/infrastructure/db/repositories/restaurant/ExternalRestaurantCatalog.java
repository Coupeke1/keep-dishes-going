package be.kdg.sa.backend.infrastructure.db.repositories.restaurant;

import be.kdg.sa.backend.domain.restaurant.Address;
import be.kdg.sa.backend.domain.restaurant.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

@Component
@Slf4j
public class ExternalRestaurantCatalog implements RestaurantCatalog {

    private final RestClient restClient;

    public ExternalRestaurantCatalog(@Qualifier("ApiRestaurantCatalog") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<Restaurant> getAllRestaurants() {
        try {
            String url = "restaurants/";

            RestaurantResponse[] response = restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(RestaurantResponse[].class);

            if (response == null) return new ArrayList<>();

            return Arrays.stream(response)
                    .map(RestaurantResponse::toRestaurant).toList();
        } catch (Exception e) {
            log.warn("Could not retrieve restaurants.", e);
            return null;
        }
    }

    @Override
    public Optional<Restaurant> getRestaurant(RestaurantId id) {
        String url = String.format("restaurants/%s", id.value());

        RestaurantResponse response = restClient
                .get()
                .uri(url)
                .retrieve()
                .body(RestaurantResponse.class);

        return Optional.ofNullable(response)
                .map(RestaurantResponse::toRestaurant);
    }

    @Override
    public boolean isRestaurantOpen(RestaurantId restaurantId) {
        try {
            String url = String.format("restaurants/%s/open-status", restaurantId.value());

            RestaurantOpenResponse response = restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(RestaurantOpenResponse.class);

            return response != null && Boolean.TRUE.equals(response.open());
        } catch (HttpStatusCodeException e) {
            log.warn("Could not check if restaurant {} is open", restaurantId, e);
            return false;
        }
    }

    @Override
    public Optional<Dish> getDish(RestaurantId restaurantId, DishId dishId) {
        try {
            String url = String.format("restaurants/%s/dishes/%s", restaurantId.value(), dishId.value());

            final DishResponse response = restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(DishResponse.class);

            return Optional.ofNullable(response)
                    .map(DishResponse::toDish);
        } catch (final HttpStatusCodeException e) {
            log.warn("Could not retrieve information of dish {}", dishId, e);
            return Optional.empty();
        }
    }

    @Override
    public List<Dish> getAllDishes(RestaurantId restaurantId) {
        String url = String.format("restaurants/%s/dishes", restaurantId.value());

        final DishResponse[] response = restClient
                .get()
                .uri(url)
                .retrieve()
                .body(DishResponse[].class);

        if (response == null) return new ArrayList<>();

        return Arrays.stream(response)
                .map(DishResponse::toDish)
                .toList();
    }

    private record DishResponse(UUID id, String name, String description, BigDecimal price, boolean vegetarian,
                                boolean vegan, boolean glutenFree, String category, String status) {
        public static Dish toDish(DishResponse dishResponse) {
            return new Dish(
                    new DishId(dishResponse.id()),
                    dishResponse.name(),
                    dishResponse.description(),
                    dishResponse.price(),
                    dishResponse.vegetarian(),
                    dishResponse.vegan(),
                    dishResponse.glutenFree(),
                    dishResponse.category(),
                    dishResponse.status
            );
        }
    }

    private record RestaurantResponse(UUID id, String name, AddressResponse address, String cuisineType,
                                      String priceIndicator, OpeningHoursResponse openingHours, boolean isOpen) {
        public static Restaurant toRestaurant(RestaurantResponse restaurantResponse) {
            return new Restaurant(
                    new RestaurantId(restaurantResponse.id),
                    restaurantResponse.name(),
                    AddressResponse.toAddress(restaurantResponse.address()),
                    restaurantResponse.cuisineType,
                    restaurantResponse.priceIndicator,
                    OpeningHoursResponse.toOpeningHours(restaurantResponse.openingHours),
                    restaurantResponse.isOpen
            );
        }
    }

    private record AddressResponse(String street, String houseNumber, String busNumber, String city,
                                   String country, String postalCode) {
        public static Address toAddress(AddressResponse addressResponse) {
            return new Address(
                    addressResponse.street,
                    addressResponse.houseNumber,
                    addressResponse.busNumber,
                    addressResponse.country,
                    addressResponse.city,
                    addressResponse.postalCode
            );
        }
    }

    private record OpeningHoursResponse(List<OpeningDayResponse> days) {
        public record OpeningPeriodResponse(LocalTime openTime, LocalTime closeTime) {
        }

        public record OpeningDayResponse(DayOfWeek day, List<OpeningPeriodResponse> periods) {
        }

        public static OpeningHours toOpeningHours(OpeningHoursResponse openingHoursResponse) {
            return new OpeningHours(openingHoursResponse.days.stream().map(day ->
                    new OpeningHours.OpeningDays(day.day, day.periods.stream().map(period ->
                            new OpeningHours.OpeningPeriod(period.openTime, period.closeTime)
                    ).toList())).toList());
        }
    }

    private record RestaurantOpenResponse(Boolean open) {
    }
}
