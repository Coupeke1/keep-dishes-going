package be.kdg.sa.backend.domain.restaurant.openingHours;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.DayOfWeek;
import java.util.List;

public record OpeningDay(DayOfWeek day, List<OpeningPeriod> periods) {
    @JsonCreator
    public OpeningDay(
            @JsonProperty("day") DayOfWeek day,
            @JsonProperty("periods") List<OpeningPeriod> periods) {
        this.day = day;
        this.periods = periods;
    }
}
