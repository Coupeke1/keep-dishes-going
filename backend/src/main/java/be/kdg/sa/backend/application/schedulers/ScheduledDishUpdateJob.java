package be.kdg.sa.backend.application.schedulers;

import be.kdg.sa.backend.application.services.restaurant.RestaurantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScheduledDishUpdateJob {
    private final RestaurantService restaurantService;

    public ScheduledDishUpdateJob(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @Scheduled(fixedRate = 60000) // every minute
    private void applyScheduledDishChanges() {
        log.debug("Running scheduled dish update job");
        restaurantService.applyScheduledDishChanges();
    }
}