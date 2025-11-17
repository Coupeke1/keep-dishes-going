package be.kdg.sa.backend.api.delivery;

import be.kdg.sa.backend.api.delivery.dto.CreateDeliveryDto;
import be.kdg.sa.backend.api.delivery.dto.DeliveryDto;
import be.kdg.sa.backend.application.services.delivery.DeliveryService;
import be.kdg.sa.backend.domain.delivery.Delivery;
import be.kdg.sa.backend.domain.delivery.OrderId;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("api/deliveries")
@Slf4j
public class DeliveryRestController {

    private final DeliveryService deliveryService;

    public DeliveryRestController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('owner', 'couriers')")
    public ResponseEntity<DeliveryDto> getDelivery(@PathVariable UUID orderId) {
        log.info("REST request to get delivery with id: {}", orderId);
        Delivery delivery = deliveryService.getById(new OrderId(orderId));
        return ResponseEntity.ok(DeliveryDto.fromDomain(delivery));
    }

    @PostMapping({"", "/"})
    @PreAuthorize("hasRole('owner')")
    public ResponseEntity<DeliveryDto> createDelivery(@Valid @RequestBody CreateDeliveryDto dto) {
        log.info("Creating delivery: {}", dto);

        Delivery created = deliveryService.createDelivery(
                UUID.randomUUID(),
                dto.pickupAddress().toDomain(),
                dto.deliveryAddress().toDomain()
        );

        return ResponseEntity.created(
                        ServletUriComponentsBuilder.fromCurrentRequest()
                                .path("/{id}")
                                .buildAndExpand(created.getOrderId().id())
                                .toUri()
                )
                .body(DeliveryDto.fromDomain(created));
    }
}
