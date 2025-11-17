package be.kdg.sa.backend.domain.order;

import be.kdg.sa.backend.domain.InvalidOrderStateException;
import be.kdg.sa.backend.domain.payment.Payment;
import be.kdg.sa.backend.domain.restaurant.Address;
import be.kdg.sa.backend.domain.restaurant.Dish;
import be.kdg.sa.backend.domain.restaurant.DishId;
import be.kdg.sa.backend.domain.restaurant.RestaurantId;
import com.mollie.mollie.models.components.Amount;
import com.mollie.mollie.models.components.PaymentRequestLines;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@AggregateRoot
@Getter
public class Order {
    @Identity
    private final OrderId id;
    private RestaurantId restaurantId;
    private LocalDateTime timePlaced;
    @Setter
    private List<OrderLine> lines = new ArrayList<>();
    private OrderStatus status;
    private BigDecimal totalPrice;
    private String decisionReason;
    private LocalDateTime decisionAt;
    private String customerName;
    private String customerEmail;
    private Address deliveryAddress;
    private Payment  payment;

    public Order(final OrderId id, final RestaurantId restaurantId, LocalDateTime timePlaced, OrderStatus status, BigDecimal totalPrice) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.timePlaced = timePlaced;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public Order(final OrderId id, final RestaurantId restaurantId, LocalDateTime timePlaced, OrderStatus status, BigDecimal totalPrice, String name, String email,Address address,Payment payment) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.timePlaced = timePlaced;
        this.status = status;
        this.totalPrice = totalPrice;
        this.customerName = name;
        this.customerEmail = email;
        this.deliveryAddress = address;
        this.payment = payment;
    }

    public Order(final OrderId id,  OrderStatus status) {
        this.id = id;
        this.status = status;
    }

    public static Order createCart() {
        return new Order(OrderId.create(), OrderStatus.CART);
    }

    public void setCustomerDetails(String name, String email,Address address) {
        if (this.status != OrderStatus.CART && this.status != OrderStatus.CUSTOMER_DETAILS_PROVIDED) {
            throw new IllegalStateException("Only orders before payment stage can set customer details");
        }
        if (this.lines.isEmpty()) {
            throw new IllegalStateException("Cannot set customer details on an empty order");
        }

        this.totalPrice = calculateTotalPrice();
        this.customerName = name;
        this.customerEmail = email;
        this.deliveryAddress = address;
        this.status = OrderStatus.CUSTOMER_DETAILS_PROVIDED;
    }

    public void assignPayment(Payment payment) {
        if (this.status != OrderStatus.CUSTOMER_DETAILS_PROVIDED) {
            throw new IllegalStateException("Only orders with customer details can set payment");
        }
        if (this.lines.isEmpty()) {
            throw new IllegalStateException("Cannot set payment on an empty order");
        }

        this.payment = payment;
        this.status = OrderStatus.PAYMENT_IN_PROGRESS;
    }

    public void confirmPayment() {
        if (payment == null || !payment.isPaid()) {
            throw new IllegalStateException("Payment is not paid");
        }
        place();
    }

    private void place() {
        if (this.status != OrderStatus.PAYMENT_IN_PROGRESS) {
            throw new IllegalStateException("Only orders with a payment can be placed");
        }
        if (this.lines.isEmpty()) {
            throw new IllegalStateException("Cannot place an empty order");
        }

        this.status = OrderStatus.PLACED;
        this.timePlaced = LocalDateTime.now();
    }

    public void addDish(final DishId dishId, final RestaurantId restaurantId, final Dish dish, final int quantity, String notes) {
        if (this.status != OrderStatus.CART) {
            throw new IllegalStateException("Only cart orders can have dishes added");
        }

        if (this.restaurantId == null) {
            this.restaurantId = restaurantId;
        } else if (!this.restaurantId.equals(restaurantId)) {
            throw new IllegalArgumentException("Cannot add dish from a different restaurant to this order");
        }

        if (dish.status() == null || !dish.status().equalsIgnoreCase("AVAILABLE")) {
            throw new IllegalStateException("Dish " + dish.name() + " is not available for ordering.");
        }

        final var existingLine = lines.stream()
                .filter(line -> line.isForDish(dishId))
                .findFirst();

        existingLine.ifPresentOrElse(
                line -> {
                    line.addQuantity(quantity);
                    if (notes != null) line.setNotes(notes);
                },
                () -> lines.add(new OrderLine(dishId, dish.name(), dish.price(), quantity, notes))
        );
        this.totalPrice = calculateTotalPrice();
    }

    private BigDecimal calculateTotalPrice() {
        return this.lines.stream().map(OrderLine::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void accept(String reason) {
        if (this.status == OrderStatus.ACCEPTED) {
            if (this.decisionReason == null && reason != null) {
                this.decisionReason = reason;
            }
            return;
        }

        if (this.status == OrderStatus.REJECTED) {
            throw new InvalidOrderStateException("Cannot accept an order that is already rejected");
        }

        if (this.status != OrderStatus.PLACED) {
            throw new InvalidOrderStateException("Order must be in PLACED state to be accepted");
        }

        this.status = OrderStatus.ACCEPTED;
        this.decisionReason = reason;
        this.decisionAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        if (this.status == OrderStatus.REJECTED) {
            if (this.decisionReason == null && reason != null) {
                this.decisionReason = reason;
            }
            return;
        }

        if (this.status == OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot reject an order that is already accepted");
        }

        if (this.status != OrderStatus.PLACED) {
            throw new IllegalStateException("Order must be in PLACED state to be rejected");
        }

        this.status = OrderStatus.REJECTED;
        this.decisionReason = reason;
        this.decisionAt = LocalDateTime.now();
    }

    public boolean isFinalized() {
        return this.status == OrderStatus.ACCEPTED || this.status == OrderStatus.REJECTED;
    }

    public void markAsReady() {
        if (this.status != OrderStatus.ACCEPTED) {
            throw new InvalidOrderStateException("Order must be in ACCEPTED state to be marked as ready");
        }
        this.status = OrderStatus.READY;
        this.decisionAt = LocalDateTime.now();
    }

    public String getTotalPriceString() {
        return String.format(Locale.US, "%.2f", this.totalPrice);
    }

    public List<PaymentRequestLines> toPaymentRequestLines() {
        return lines.stream()
                .map(line -> PaymentRequestLines.builder()
                        .description(line.getDishName())
                        .quantity(line.getQuantity())
                        .unitPrice(Amount.builder()
                                .currency("EUR")
                                .value(String.format(Locale.US, "%.2f", line.getUnitPrice()))
                                .build())
                        .totalAmount(Amount.builder()
                                .currency("EUR")
                                .value(String.format(Locale.US, "%.2f", line.getTotalPrice()))
                                .build())
                        .build())
                .toList();
    }

    public void markAsOutForDelivery() {
        if (this.status != OrderStatus.READY) {
            throw new InvalidOrderStateException("Order must be in READY state to be marked as picked-up");
        }
        this.status = OrderStatus.PICKED_UP;
    }

    public void markAsDelivered() {
        if (this.status != OrderStatus.PICKED_UP) {
            throw new InvalidOrderStateException("Order must be in PICKED_UP state to be marked as delivered");
        }
        this.status = OrderStatus.DELIVERED;
    }
}
