package be.kdg.sa.backend.application.services.restaurant;

import be.kdg.sa.backend.api.dto.AddressDto;
import be.kdg.sa.backend.api.dto.restaurant.RestaurantOrderDto;
import be.kdg.sa.backend.application.publisher.OrderEventPublisher;
import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.NotFoundException;
import be.kdg.sa.backend.domain.restaurant.Restaurant;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import be.kdg.sa.backend.domain.restaurantOrder.OrderStatus;
import be.kdg.sa.backend.domain.restaurantOrder.RestaurantOrder;
import be.kdg.sa.backend.domain.restaurantOrder.RestaurantOrderId;
import be.kdg.sa.backend.events.OrderCreatedEvent;
import be.kdg.sa.backend.events.OrderDecisionEvent;
import be.kdg.sa.backend.events.OrderReadyPublishedEvent;
import be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository.RestaurantOrderRepository;
import be.kdg.sa.backend.infrastructure.db.repositories.restaurantRepository.RestaurantRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
public class RestaurantOrderService {
    private final RestaurantOrderRepository restaurantOrderRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderEventPublisher eventPublisher;

    public RestaurantOrderService(RestaurantOrderRepository restaurantOrderRepository, RestaurantRepository restaurantRepository, OrderEventPublisher eventPublisher) {
        this.restaurantOrderRepository = restaurantOrderRepository;
        this.restaurantRepository = restaurantRepository;
        this.eventPublisher = eventPublisher;
    }


    public Optional<RestaurantOrderDto> getOrder(UUID orderId) {
        return restaurantOrderRepository.findById(orderId).map(this::toDto);
    }

    public List<RestaurantOrderDto> getPendingOrders(UUID restaurantId) {
        return restaurantOrderRepository.findByRestaurantIdAndStatus(restaurantId, "PLACED")
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private RestaurantOrderDto toDto(RestaurantOrder entity) {
        Instant createdAt = null;
        if (entity.getCreatedAt() != null) {
            createdAt = entity.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant();
        }

        AddressDto deliveryAddress = new AddressDto(
                entity.getDeliveryAddress().street(),
                entity.getDeliveryAddress().houseNumber(),
                entity.getDeliveryAddress().busNumber(),
                entity.getDeliveryAddress().postalCode(),
                entity.getDeliveryAddress().city(),
                entity.getDeliveryAddress().country()
        );

        return new RestaurantOrderDto(
                entity.getOrderId().id(),
                entity.getRestaurantId().id(),
                entity.getStatus().name(),
                entity.getDecisionReason(),
                createdAt,
                entity.getCustomerName(),
                entity.getCustomerEmail(),
                deliveryAddress
        );
    }

    public boolean applyDecision(UUID orderId, String decision, String reason) {
        RestaurantOrder entity = restaurantOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (OrderStatus.PLACED != entity.getStatus()) {
            return false;
        }

        if ("ACCEPTED".equalsIgnoreCase(decision)) {
            entity.accept(reason);
        } else {
            entity.reject(reason);
        }

        restaurantOrderRepository.save(entity);

        Restaurant restaurant = restaurantRepository.findById(new RestaurantId(entity.getRestaurantId().id()))
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));

        OrderDecisionEvent.Address address = new OrderDecisionEvent.Address(
                restaurant.getAddress().street(),
                restaurant.getAddress().houseNumber(),
                restaurant.getAddress().busNumber(),
                restaurant.getAddress().postalCode(),
                restaurant.getAddress().city(),
                restaurant.getAddress().country()
        );

        var event = new OrderDecisionEvent(orderId, decision, reason, address);
        eventPublisher.publishOrderDecision(event);

        return true;
    }

    public boolean markReadyForPickup(UUID orderId) {
        RestaurantOrder entity = restaurantOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (OrderStatus.ACCEPTED != entity.getStatus()) {
            return false;
        }

        entity.markReady();
        restaurantOrderRepository.save(entity);

        Restaurant restaurant = restaurantRepository.findById(new RestaurantId(entity.getRestaurantId().id()))
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));

        OrderReadyPublishedEvent readyEvt = new OrderReadyPublishedEvent(orderId, restaurant.getId().id());

        eventPublisher.publishOrderReady(readyEvt);

        return true;
    }

    public void createOrderFromEvent(OrderCreatedEvent event) {

        if (restaurantOrderRepository.existsById(event.orderId())) {
            log.info("Order {} already exists — ignoring", event.orderId());
            return;
        }
        var address = new Address(
                event.customer().address().street(),
                event.customer().address().houseNumber(),
                event.customer().address().busNumber(),
                event.customer().address().postalCode(),
                event.customer().address().city(),
                event.customer().address().country()
        );

        var restaurantOrder = RestaurantOrder.reconstruct(
                new RestaurantOrderId(event.orderId()),
                new RestaurantId(event.restaurantId()),
                event.customer().name(),
                event.customer().email(),
                address,
                OrderStatus.PLACED,
                null,
                LocalDateTime.now()
        );

        restaurantOrderRepository.save(restaurantOrder);

        log.info("Saved new RestaurantOrder {} for restaurant {}", event.orderId(), event.restaurantId());
    }
}
