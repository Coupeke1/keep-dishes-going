package be.kdg.sa.backend.domain.restaurant.openingHours;

import java.time.LocalTime;

public record OpeningPeriod(LocalTime openTime, LocalTime closeTime) {
}
