package be.kdg.sa.backend.domain.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public record PayoutCalculator(BigDecimal baseFee, BigDecimal perMinuteRate, int minMinutes, int maxMinutes) {
    public PayoutCalculator(BigDecimal baseFee, BigDecimal perMinuteRate, int minMinutes, int maxMinutes) {
        this.baseFee = Objects.requireNonNull(baseFee, "baseFee must not be null");
        this.perMinuteRate = Objects.requireNonNull(perMinuteRate, "perMinuteRate must not be null");
        this.minMinutes = minMinutes;
        this.maxMinutes = maxMinutes;
    }

    public BigDecimal calculateFor(LocalDateTime pickupTime, LocalDateTime deliveryTime) {
        if (pickupTime == null || deliveryTime == null)
            throw new IllegalArgumentException("Tijden mogen niet null zijn");
        if (deliveryTime.isBefore(pickupTime))
            throw new IllegalArgumentException("deliveryTime moet na pickupTime zijn");

        long seconds = Duration.between(pickupTime, deliveryTime).getSeconds();
        long minutes = (seconds + 59) / 60;
        if (minutes < minMinutes) minutes = minMinutes;
        if (minutes > maxMinutes) minutes = maxMinutes;

        BigDecimal total = baseFee.add(perMinuteRate.multiply(BigDecimal.valueOf(minutes)));
        return total.setScale(2, RoundingMode.CEILING);
    }
}
