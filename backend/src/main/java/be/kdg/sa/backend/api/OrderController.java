package be.kdg.sa.backend.api;

import be.kdg.sa.backend.api.dto.order.AddDishDto;
import be.kdg.sa.backend.api.dto.order.OrderDto;
import be.kdg.sa.backend.api.dto.order.PlaceOrderDto;
import be.kdg.sa.backend.application.OrderService;
import be.kdg.sa.backend.application.PaymentService;
import be.kdg.sa.backend.domain.order.Order;
import be.kdg.sa.backend.domain.order.OrderId;
import be.kdg.sa.backend.domain.restaurant.Address;
import be.kdg.sa.backend.domain.restaurant.DishId;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orders;
    private final PaymentService payments;

    public OrderController(final OrderService orders, final PaymentService payments) {
        this.orders = orders;
        this.payments = payments;
    }

    @GetMapping({"/", ""})
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<OrderDto>> findAll(
            @RequestParam(name = "dishId", required = false) @Nullable UUID dishId
    ) {
        boolean hasDishId = (dishId != null);

        List<Order> orders;
        if (hasDishId) {
            orders = this.orders.findByDishId(new DishId(dishId));
        } else {
            orders = this.orders.findAll();
        }

        List<OrderDto> orderDtos = orders.stream()
                .map(OrderDto::from)
                .toList();

        return ResponseEntity.ok(orderDtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<OrderDto> find(@PathVariable("id") final UUID id) {
        final Order order = orders.findById(new OrderId(id));
        final OrderDto orderDto = OrderDto.from(order);
        return ResponseEntity.ok(orderDto);
    }

    @PostMapping({"/", ""})
    @PreAuthorize("permitAll()")
    public ResponseEntity<OrderDto> create() {
        final Order order = orders.create();
        final OrderDto orderDto = OrderDto.from(order);
        return ResponseEntity.ok(orderDto);
    }

    @PostMapping("/{id}/dishes")
    @PreAuthorize("permitAll()")
    public ResponseEntity<OrderDto> addDish(
            @PathVariable("id") final UUID id,
            @Valid @RequestBody final AddDishDto dto
    ) {
        final OrderId orderId = new OrderId(id);
        final DishId dishId = new DishId(dto.dishId());
        final RestaurantId restaurantId = new RestaurantId(dto.restaurantId());
        final Order order = orders.addDish(orderId, restaurantId, dishId, dto.quantity(), dto.notes());
        return ResponseEntity.ok(OrderDto.from(order));
    }

    @PostMapping("/{id}/customer-details")
    @PreAuthorize("permitAll()")
    public ResponseEntity<OrderDto> customerDetails(
            @PathVariable("id") final UUID id,
            @Valid @RequestBody final PlaceOrderDto dto
    ) {
        final OrderId orderId = new OrderId(id);

        final Order order = orders.setCustomerDetails(
                orderId,
                dto.name(),
                dto.email(), new Address(dto.addressDto().street(),
                        dto.addressDto().houseNumber(),
                        dto.addressDto().busNumber(),
                        dto.addressDto().country(),
                        dto.addressDto().city(),
                        dto.addressDto().postalCode())
        );

        return ResponseEntity.ok(OrderDto.from(order));
    }

    @PostMapping("/{orderId}/create-payment")
    @PreAuthorize("permitAll()")
    public ResponseEntity<OrderDto> create(@PathVariable UUID orderId) {
        Order orderWithPayment = payments.createPayment(new OrderId(orderId));
        OrderDto orderDto = OrderDto.from(orderWithPayment);
        return ResponseEntity.ok(orderDto);
    }

    @PostMapping("/payments/webhook")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> webhook(
            @RequestParam(value = "payment-id", required = false) String paymentId,
            @RequestParam(value = "order-id", required = false) UUID orderId) {

        if (paymentId != null) {
            payments.handleWebhook(paymentId);
        } else if (orderId != null) {
            payments.handleWebhook(new OrderId(orderId));
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }
}
