package be.kdg.sa.backend.domain.restaurant;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record OpeningHours(List<OpeningDays> days) {
    public record OpeningDays(DayOfWeek day, List<OpeningPeriod> periods) {}
    public record OpeningPeriod(LocalTime openTime, LocalTime closeTime) {}
}
