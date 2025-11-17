package be.kdg.sa.backend.application.services.restaurant;

import be.kdg.sa.backend.api.dto.restaurant.RestaurantOrderDto;
import be.kdg.sa.backend.application.publisher.OrderEventPublisher;
import be.kdg.sa.backend.domain.Address;
import be.kdg.sa.backend.domain.NotFoundException;
import be.kdg.sa.backend.domain.owner.OwnerId;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantOrderServiceTest {

    @Mock
    private RestaurantOrderRepository orderRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private RestaurantOrderService sut;

    @Captor
    private ArgumentCaptor<OrderDecisionEvent> decisionEventCaptor;

    @Captor
    private ArgumentCaptor<OrderReadyPublishedEvent> readyEventCaptor;

    private RestaurantId restaurantId;
    private UUID orderId;
    private Address address;
    private OwnerId ownerId;

    @BeforeEach
    void setup() {
        ownerId = new OwnerId(UUID.randomUUID());
        restaurantId = RestaurantId.create();
        orderId = UUID.randomUUID();
        address = new Address("Street", "1", "A", "1000", "City", "Belgium");
    }

    @Test
    void getOrderExists() {
        RestaurantOrder order = RestaurantOrder.reconstruct(
                new RestaurantOrderId(orderId),
                restaurantId,
                "John",
                "john@test.com",
                address,
                OrderStatus.PLACED,
                null,
                LocalDateTime.now()
        );
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        Optional<RestaurantOrderDto> result = sut.getOrder(orderId);

        assertThat(result).isPresent();
        assertThat(result.get().orderId()).isEqualTo(orderId);
    }

    @Test
    void getOrderNotExists() {
        given(orderRepository.findById(orderId)).willReturn(Optional.empty());

        Optional<RestaurantOrderDto> result = sut.getOrder(orderId);

        assertThat(result).isEmpty();
    }

    @Test
    void getPendingOrdersReturnsList() {
        RestaurantOrder order = RestaurantOrder.create(restaurantId, "John", "john@test.com", address);
        given(orderRepository.findByRestaurantIdAndStatus(restaurantId.id(), "PLACED"))
                .willReturn(List.of(order));

        List<RestaurantOrderDto> result = sut.getPendingOrders(restaurantId.id());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().status()).isEqualTo("PLACED");
    }

    @Test
    void applyDecisionAccepted() {
        RestaurantOrder order = RestaurantOrder.create(restaurantId, "John", "john@test.com", address);
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        Restaurant restaurant = Restaurant.create("R", address, "123", "a@b.com", "Cuisine", ownerId);
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));

        boolean result = sut.applyDecision(orderId, "ACCEPTED", "Good");

        assertTrue(result);
        assertEquals(OrderStatus.ACCEPTED, order.getStatus());
        verify(orderRepository).save(order);
        verify(eventPublisher).publishOrderDecision(decisionEventCaptor.capture());
        assertThat(decisionEventCaptor.getValue().decision()).isEqualTo("ACCEPTED");
    }

    @Test
    void applyDecisionRejected() {
        RestaurantOrder order = RestaurantOrder.create(restaurantId, "John", "john@test.com", address);
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        Restaurant restaurant = Restaurant.create("R", address, "123", "a@b.com", "Cuisine", ownerId);
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));

        boolean result = sut.applyDecision(orderId, "REJECTED", "Bad");

        assertTrue(result);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).save(order);
        verify(eventPublisher).publishOrderDecision(decisionEventCaptor.capture());
        assertThat(decisionEventCaptor.getValue().decision()).isEqualTo("REJECTED");
    }

    @Test
    void applyDecisionWrongStatus() {
        RestaurantOrder order = RestaurantOrder.create(restaurantId, "John", "john@test.com", address);
        order.accept("initial");
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        boolean result = sut.applyDecision(orderId, "ACCEPTED", "Another");

        assertFalse(result);
        verify(orderRepository, never()).save(order);
        verify(eventPublisher, never()).publishOrderDecision(any());
    }

    @Test
    void applyDecisionOrderNotFound() {
        given(orderRepository.findById(orderId)).willReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> sut.applyDecision(orderId, "ACCEPTED", "X"));
    }

    @Test
    void markReadyForPickupSuccess() {
        RestaurantOrder order = RestaurantOrder.create(restaurantId, "John", "john@test.com", address);
        order.accept("ok");
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        Restaurant restaurant = Restaurant.create("R", address, "123", "a@b.com", "Cuisine", ownerId);
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));

        boolean result = sut.markReadyForPickup(orderId);

        assertTrue(result);
        assertEquals(OrderStatus.READY, order.getStatus());
        verify(orderRepository).save(order);
        verify(eventPublisher).publishOrderReady(readyEventCaptor.capture());
        assertThat(readyEventCaptor.getValue().orderId()).isEqualTo(orderId);
    }

    @Test
    void markReadyForPickupWrongStatus() {
        RestaurantOrder order = RestaurantOrder.create(restaurantId, "John", "john@test.com", address);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        boolean result = sut.markReadyForPickup(orderId);

        assertFalse(result);
        verify(orderRepository, never()).save(order);
        verify(eventPublisher, never()).publishOrderReady(any());
    }

    @Test
    void markReadyForPickupOrderNotFound() {
        given(orderRepository.findById(orderId)).willReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> sut.markReadyForPickup(orderId));
    }

    @Test
    void createOrderFromEventNewOrder() {
        OrderCreatedEvent.Customer cust = new OrderCreatedEvent.Customer("John", "john@test.com",
                new OrderCreatedEvent.Address("Street", "1", "Belgium", "City", "1000", "A"));
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, restaurantId.id(), cust, List.of(), "now", null);

        given(orderRepository.existsById(orderId)).willReturn(false);

        sut.createOrderFromEvent(event);

        verify(orderRepository).save(any(RestaurantOrder.class));
    }

    @Test
    void createOrderFromEventExistingOrder() {
        OrderCreatedEvent.Customer cust = new OrderCreatedEvent.Customer("John", "john@test.com",
                new OrderCreatedEvent.Address("Street", "1", "Belgium", "City", "1000", "A"));
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, restaurantId.id(), cust, List.of(), "now", null);

        given(orderRepository.existsById(orderId)).willReturn(true);

        sut.createOrderFromEvent(event);

        verify(orderRepository, never()).save(any());
    }
}
