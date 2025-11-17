package be.kdg.sa.backend.domain.restaurant.openingHours;

import org.jmolecules.ddd.annotation.ValueObject;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@ValueObject
public record OpeningHours(List<OpeningDay> days) {

    public boolean isOpenAt(DayOfWeek day, LocalTime time) {
        return days.stream()
                .filter(d -> d.day() == day)
                .flatMap(d -> d.periods().stream())
                .anyMatch(p -> !time.isBefore(p.openTime()) && !time.isAfter(p.closeTime()));
    }
}
