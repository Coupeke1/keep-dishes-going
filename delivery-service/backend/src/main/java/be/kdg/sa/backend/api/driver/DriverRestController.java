package be.kdg.sa.backend.api.driver;

import be.kdg.sa.backend.api.delivery.dto.CompleteDeliveryDto;
import be.kdg.sa.backend.api.delivery.dto.CompletedDeliveriesResponse;
import be.kdg.sa.backend.api.driver.dto.CreateDriverDto;
import be.kdg.sa.backend.api.driver.dto.DriverDto;
import be.kdg.sa.backend.application.services.driver.DriverService;
import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.driver.Driver;
import be.kdg.sa.backend.domain.driver.DriverId;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("api/drivers")
@Slf4j
public class DriverRestController {

    private final DriverService driverService;

    public DriverRestController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('couriers')")
    public ResponseEntity<DriverDto> getCurrentDriver(@AuthenticationPrincipal Jwt principal) {
        UUID driverUuid = UUID.fromString(principal.getSubject());
        log.info("REST request to get Driver info for Keycloak ID {}", driverUuid);

        Driver driver = driverService.getById(new DriverId(driverUuid));
        return ResponseEntity.ok(DriverDto.fromDomain(driver));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('couriers')")
    public ResponseEntity<DriverDto> registerDriver(
            @Valid @RequestBody CreateDriverDto dto,
            @AuthenticationPrincipal Jwt principal
    ) {
        UUID driverUuid = UUID.fromString(principal.getSubject());
        log.info("Registering driver with Keycloak ID {} and data {}", driverUuid, dto);

        Address address = dto.address().toDomain();

        Driver created = driverService.registerDriver(
                new DriverId(driverUuid),
                dto.name(),
                dto.email(),
                dto.phoneNumber(),
                dto.accountNumber(),
                address
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/me")
                .build()
                .toUri();

        return ResponseEntity.created(location).body(DriverDto.fromDomain(created));
    }

    @PostMapping("/claim/{deliveryId}")
    @PreAuthorize("hasRole('couriers')")
    public ResponseEntity<Void> claimDelivery(@PathVariable UUID deliveryId, @AuthenticationPrincipal Jwt principal) {
        UUID driverUuid = UUID.fromString(principal.getSubject());
        log.info("Driver {} claiming delivery {}", driverUuid, deliveryId);

        driverService.claimDelivery(driverUuid, deliveryId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/pickup")
    @PreAuthorize("hasRole('couriers')")
    public ResponseEntity<Void> startPickup(@AuthenticationPrincipal Jwt principal) {
        UUID driverUuid = UUID.fromString(principal.getSubject());
        log.info("Driver {} starting pickup", driverUuid);

        driverService.startDelivery(driverUuid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/complete")
    @PreAuthorize("hasRole('couriers')")
    public ResponseEntity<CompleteDeliveryDto> completeDelivery(@AuthenticationPrincipal Jwt principal) {
        UUID driverUuid = UUID.fromString(principal.getSubject());
        log.info("Driver {} completing delivery", driverUuid);

        BigDecimal payout = driverService.completeDelivery(driverUuid);
        return ResponseEntity.ok(new CompleteDeliveryDto(driverUuid, payout));
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasRole('couriers')")
    public ResponseEntity<Void> cancelDelivery(@AuthenticationPrincipal Jwt principal) {
        UUID driverUuid = UUID.fromString(principal.getSubject());
        log.info("Driver {} cancelling delivery", driverUuid);

        driverService.cancelDelivery(driverUuid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/completed-deliveries")
    @PreAuthorize("hasRole('couriers')")
    public ResponseEntity<CompletedDeliveriesResponse> getCompletedDeliveries(@AuthenticationPrincipal Jwt principal) {
        UUID driverUuid = UUID.fromString(principal.getSubject());
        log.info("Get completed deliveries for driver {}", driverUuid);

        CompletedDeliveriesResponse response = driverService.getCompletedDeliveriesForDriver(new DriverId(driverUuid));
        return ResponseEntity.ok(response);
    }
}
