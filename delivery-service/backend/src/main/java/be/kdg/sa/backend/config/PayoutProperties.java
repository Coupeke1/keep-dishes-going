package be.kdg.sa.backend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Component
public class PayoutProperties {
    @Value("${PAYOUT_BASE_FEE:3.00}")
    private BigDecimal baseFee;

    @Value("${PAYOUT_PER_MINUTE:0.30}")
    private BigDecimal perMinute;

    @Value("${PAYOUT_MIN_MINUTES:5}")
    private int minMinutes;

    @Value("${PAYOUT_MAX_MINUTES:30}")
    private int maxMinutes;

}
