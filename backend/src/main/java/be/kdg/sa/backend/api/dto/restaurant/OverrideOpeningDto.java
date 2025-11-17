package be.kdg.sa.backend.api.dto.restaurant;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record OverrideOpeningDto(
        @NotNull(message = "Open flag is required")
        Boolean open,
        LocalDateTime until
) {
}
