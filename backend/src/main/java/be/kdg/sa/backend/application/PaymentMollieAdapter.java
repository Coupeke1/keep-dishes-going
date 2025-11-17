package be.kdg.sa.backend.application;

import be.kdg.sa.backend.domain.order.Order;
import be.kdg.sa.backend.domain.payment.MolliePaymentId;
import be.kdg.sa.backend.domain.payment.Payment;
import com.mollie.mollie.Client;
import com.mollie.mollie.models.components.*;
import com.mollie.mollie.models.operations.CreatePaymentResponse;
import com.mollie.mollie.models.operations.GetPaymentRequest;
import com.mollie.mollie.models.operations.GetPaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentMollieAdapter {
    private final Client mollieClient;
    private final String baseUrl;
    public PaymentMollieAdapter(@Value("${mollie.api.key}") String apiKey, @Value("${frontend.url}") String baseUrl) {
        this.mollieClient = Client.builder()
                .security(Security.builder()
                        .apiKey(apiKey)
                        .build())
                .build();
        this.baseUrl = baseUrl;
    }

    public Payment createPaymentForOrder(Order order) {
        PaymentRequest request = PaymentRequest.builder()
                .description("Order " + order.getId().value())
                .amount(Amount.builder()
                        .currency("EUR")
                        .value(order.getTotalPriceString())
                        .build())
                .lines(order.toPaymentRequestLines())
                .redirectUrl(baseUrl + "/order/" + order.getId().value())
                .sequenceType(SequenceType.ONEOFF)
                .digitalGoods(false)
                .build();

        CreatePaymentResponse response = mollieClient.payments().create()
                .paymentRequest(request)
                .call();

        MolliePaymentId id = new MolliePaymentId(response.paymentResponse().orElseThrow().id().orElseThrow());
        String url = response.paymentResponse().orElseThrow().links().orElseThrow().checkout().orElseThrow().href();

        return new Payment(id, url);
    }

    public void confirmPayment(Payment payment) {
        String paymentId = payment.getId().value();
        GetPaymentRequest request = GetPaymentRequest.builder().paymentId(paymentId).build();
        GetPaymentResponse response = mollieClient.payments().get(request);

        if (response.paymentResponse().isEmpty() || !response.paymentResponse().get().status().orElseThrow().equals(PaymentStatus.PAID)) {
            throw new IllegalStateException("Payment not completed");
        }

        payment.markAsPaid();
    }
}
