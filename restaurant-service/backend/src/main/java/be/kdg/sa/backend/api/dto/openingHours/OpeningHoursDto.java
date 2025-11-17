package be.kdg.sa.backend.api.dto.openingHours;

import be.kdg.sa.backend.domain.restaurant.openingHours.OpeningHours;

import java.util.List;

public record OpeningHoursDto(
        List<OpeningDayDto> days
) {
    public static OpeningHoursDto from(OpeningHours openingHours) {
        return (new OpeningHoursDto(
                openingHours.days().stream()
                        .map(day -> new OpeningDayDto(
                                day.day().toString(),
                                day.periods().stream()
                                        .map(period -> new OpeningPeriodDto(
                                                period.openTime().toString(),
                                                period.closeTime().toString()
                                        ))
                                        .toList()
                        ))
                        .toList()
        ));
    }

    public record OpeningPeriodDto(
            String openTime,
            String closeTime
    ) {
    }

    public record OpeningDayDto(
            String day,
            List<OpeningPeriodDto> periods
    ) {
    }
}

