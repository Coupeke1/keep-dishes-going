package be.kdg.sa.backend.application.services.payment;

import be.kdg.sa.backend.config.PayoutProperties;
import be.kdg.sa.backend.domain.payment.PayoutCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Slf4j
@Service
public class PayoutService {
    private final PayoutCalculator calculator;

    public PayoutService(PayoutProperties props) {
        this.calculator = new PayoutCalculator(
                props.getBaseFee(),
                props.getPerMinute(),
                props.getMinMinutes(),
                props.getMaxMinutes()
        );
    }

    public BigDecimal calculatePayout(LocalDateTime pickupTime, LocalDateTime deliveryTime) {
        return calculator.calculateFor(pickupTime, deliveryTime);
    }
}

