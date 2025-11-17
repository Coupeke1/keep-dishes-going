package be.kdg.sa.backend.api.delivery.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CompletedDeliveryItemDto {
    private UUID orderId;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private BigDecimal payout;
    private BigDecimal cumulativeTotal;

    public CompletedDeliveryItemDto(UUID orderId, LocalDateTime pickupTime, LocalDateTime deliveryTime, BigDecimal payout, BigDecimal cumulativeTotal) {
        this.orderId = orderId;
        this.pickupTime = pickupTime;
        this.deliveryTime = deliveryTime;
        this.payout = payout;
        this.cumulativeTotal = cumulativeTotal;
    }
}
