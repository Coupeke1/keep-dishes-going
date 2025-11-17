package be.kdg.sa.backend.api.dto.openingHours;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CreateOpeningPeriodDto(
        @NotNull LocalTime openTime,
        @NotNull LocalTime closeTime
) {
}
