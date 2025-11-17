package be.kdg.sa.backend.api.dto.openingHours;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.util.List;

public record CreateOpeningDayDto(
        @NotNull DayOfWeek day,
        @NotNull List<CreateOpeningPeriodDto> periods
) {
}
