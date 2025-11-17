package be.kdg.sa.backend.api;

import be.kdg.sa.backend.api.dto.openingHours.CreateOpeningHoursDto;
import be.kdg.sa.backend.api.dto.owner.OwnerOverviewDto;
import be.kdg.sa.backend.application.services.restaurant.RestaurantService;
import be.kdg.sa.backend.domain.Address;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(originPatterns = "http://localhost:[*]")
@RestController
@RequestMapping("api/owners")
public class OwnerRestController {

    private final RestaurantService restaurantService;

    public OwnerRestController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<OwnerOverviewDto> getOverview(@AuthenticationPrincipal Jwt principal) {
        UUID ownerId = UUID.fromString(principal.getSubject());
        return ResponseEntity.ok(restaurantService.getOverviewForOwner(ownerId));
    }

    @PostMapping("/restaurant")
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<Void> createRestaurant(@AuthenticationPrincipal Jwt principal,
                                                 @RequestBody CreateRestaurantRequest request) {
        UUID ownerId = UUID.fromString(principal.getSubject());
        restaurantService.create(
                ownerId,
                request.name(),
                request.address(),
                request.phone(),
                request.email(),
                request.cuisineType(),
                request.openingHours()
        );
        return ResponseEntity.ok().build();
    }

    public record CreateRestaurantRequest(
            String name,
            Address address,
            String phone,
            String email,
            String cuisineType,
            CreateOpeningHoursDto openingHours
    ) {
    }
}
