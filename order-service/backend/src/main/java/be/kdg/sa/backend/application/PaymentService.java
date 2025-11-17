package be.kdg.sa.backend.application;

import be.kdg.sa.backend.application.events.OrderPlacedDomainEvent;
import be.kdg.sa.backend.domain.NotFoundException;
import be.kdg.sa.backend.domain.order.Order;
import be.kdg.sa.backend.domain.order.OrderId;
import be.kdg.sa.backend.domain.order.OrderRepository;
import be.kdg.sa.backend.domain.payment.Payment;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PaymentService {
    private final OrderRepository orders;
    private final PaymentMollieAdapter paymentAdapter;
    private final ApplicationEventPublisher publisher;


    public PaymentService(OrderRepository orders, PaymentMollieAdapter paymentAdapter, ApplicationEventPublisher publisher) {
        this.orders = orders;
        this.paymentAdapter = paymentAdapter;
        this.publisher = publisher;
    }

    public Order createPayment(OrderId orderId) {
        Order order = orders.findById(orderId).orElseThrow(orderId::notFound);
        if (order.getPayment() != null) return order;

        Payment payment = paymentAdapter.createPaymentForOrder(order);
        order.assignPayment(payment);

        orders.save(order);
        return order;
    }

    public void handleWebhook(String molliePaymentId) {
        Order order = orders.findByPaymentId(molliePaymentId).orElseThrow(() -> new NotFoundException("Order not found"));
        handleWebhook(order);
    }

    public void handleWebhook(OrderId orderId) {
        Order order = orders.findById(orderId).orElseThrow(orderId::notFound);
        handleWebhook(order);
    }

    private void handleWebhook(Order order) {
        paymentAdapter.confirmPayment(order.getPayment());
        order.confirmPayment();


        orders.save(order);

        publisher.publishEvent(new OrderPlacedDomainEvent(
                order.getId().value(),
                order.getRestaurantId().value()
        ));
    }
}