package be.kdg.sa.backend.domain.order;

public enum OrderStatus {
    CART,
    CUSTOMER_DETAILS_PROVIDED,
    PAYMENT_IN_PROGRESS,
    PLACED,
    ACCEPTED,
    READY,
    PICKED_UP,
    DELIVERED,
    REJECTED
}
