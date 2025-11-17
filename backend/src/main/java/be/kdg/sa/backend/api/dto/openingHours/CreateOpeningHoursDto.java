package be.kdg.sa.backend.api.dto.openingHours;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOpeningHoursDto(
        @NotNull List<CreateOpeningDayDto> days
) {
}

