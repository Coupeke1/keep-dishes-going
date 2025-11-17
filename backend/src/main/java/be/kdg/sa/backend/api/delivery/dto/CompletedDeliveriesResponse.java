package be.kdg.sa.backend.api.delivery.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CompletedDeliveriesResponse {
    private UUID driverId;
    private BigDecimal total;
    private String currency = "EUR";
    private List<CompletedDeliveryItemDto> deliveries;

    public CompletedDeliveriesResponse(UUID driverId, BigDecimal total, List<CompletedDeliveryItemDto> deliveries) {
        this.driverId = driverId;
        this.total = total;
        this.deliveries = deliveries;
    }
}
