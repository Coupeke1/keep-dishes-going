package be.kdg.sa.backend.api.delivery.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class CompleteDeliveryDto {
    private UUID driverId;
    private BigDecimal payout;
    private String currency = "EUR";

    public CompleteDeliveryDto(UUID driverId, BigDecimal payout) {
        this.driverId = driverId;
        this.payout = payout;
    }

}