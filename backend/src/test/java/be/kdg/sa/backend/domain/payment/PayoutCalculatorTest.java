package be.kdg.sa.backend.domain.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PayoutCalculatorTest {

    private PayoutCalculator calculator;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;

    @BeforeEach
    void setUp() {
        calculator = new PayoutCalculator(
                new BigDecimal("3.00"),
                new BigDecimal("0.30"),
                5,
                30
        );
        pickupTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        deliveryTime = LocalDateTime.of(2024, 1, 1, 10, 15);
    }

    @Test
    void constructor_WithNullBaseFee_ShouldThrowNullPointerException() {
        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new PayoutCalculator(null, new BigDecimal("0.30"), 5, 30));
        assertNotNull(exception.getMessage());
    }

    @Test
    void constructor_WithNullPerMinuteRate_ShouldThrowNullPointerException() {
        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> new PayoutCalculator(new BigDecimal("3.00"), null, 5, 30));
        assertNotNull(exception.getMessage());
    }

    @Test
    void calculateFor_WithValidTimes_ShouldReturnCorrectPayout() {
        // Act
        BigDecimal result = calculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("7.50"), result);
    }

    @Test
    void calculateFor_WithNullPickupTime_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateFor(null, deliveryTime));
        assertEquals("Tijden mogen niet null zijn", exception.getMessage());
    }

    @Test
    void calculateFor_WithNullDeliveryTime_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateFor(pickupTime, null));
        assertEquals("Tijden mogen niet null zijn", exception.getMessage());
    }

    @Test
    void calculateFor_WithDeliveryTimeBeforePickupTime_ShouldThrowIllegalArgumentException() {
        // Arrange
        LocalDateTime earlierTime = pickupTime.minusMinutes(10);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateFor(pickupTime, earlierTime));
        assertEquals("deliveryTime moet na pickupTime zijn", exception.getMessage());
    }

    @Test
    void calculateFor_WithDeliveryTimeEqualToPickupTime_ShouldApplyMinimumMinutes() {
        // Arrange
        deliveryTime = pickupTime;

        // Act
        BigDecimal result = calculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("4.50"), result);
    }

    @Test
    void calculateFor_WithVeryShortDuration_ShouldApplyMinimumMinutes() {
        // Arrange
        deliveryTime = pickupTime.plusMinutes(2);

        // Act
        BigDecimal result = calculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("4.50"), result);
    }

    @Test
    void calculateFor_WithVeryLongDuration_ShouldApplyMaximumMinutes() {
        // Arrange
        deliveryTime = pickupTime.plusMinutes(90);

        // Act
        BigDecimal result = calculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("12.00"), result);
    }

    @Test
    void calculateFor_WithExactMinimumMinutes_ShouldCalculateCorrectly() {
        // Arrange
        deliveryTime = pickupTime.plusMinutes(5);

        // Act
        BigDecimal result = calculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("4.50"), result);
    }

    @Test
    void calculateFor_WithExactMaximumMinutes_ShouldCalculateCorrectly() {
        // Arrange
        deliveryTime = pickupTime.plusMinutes(30);

        // Act
        BigDecimal result = calculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("12.00"), result);
    }

    @Test
    void calculateFor_WithPartialMinutes_ShouldRoundUp() {
        // Arrange
        deliveryTime = pickupTime.plusMinutes(15).plusSeconds(30);

        // Act
        BigDecimal result = calculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("7.80"), result);
    }

    @Test
    void calculateFor_WithOneSecond_ShouldRoundUpToOneMinute() {
        // Arrange
        deliveryTime = pickupTime.plusSeconds(1);

        // Act
        BigDecimal result = calculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("4.50"), result);
    }

    @Test
    void calculateFor_With59Seconds_ShouldRoundUpToOneMinute() {
        // Arrange
        deliveryTime = pickupTime.plusSeconds(59);

        // Act
        BigDecimal result = calculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("4.50"), result);
    }

    @Test
    void calculateFor_With60Seconds_ShouldCalculateAsOneMinute() {
        // Arrange
        deliveryTime = pickupTime.plusSeconds(60);

        // Act
        BigDecimal result = calculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("4.50"), result);
    }

    @Test
    void calculateFor_WithZeroSeconds_ShouldNotRoundUp() {
        // Arrange
        deliveryTime = pickupTime.plusMinutes(15);

        // Act
        BigDecimal result = calculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("7.50"), result);
    }

    @Test
    void calculateFor_WithDifferentCalculatorParameters_ShouldCalculateCorrectly() {
        // Arrange
        PayoutCalculator customCalculator = new PayoutCalculator(
                new BigDecimal("5.00"),
                new BigDecimal("0.50"),
                10,
                45
        );
        deliveryTime = pickupTime.plusMinutes(20);

        // Act
        BigDecimal result = customCalculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("15.00"), result);
    }

    @Test
    void calculateFor_WithDurationBelowMinimum_ShouldApplyMinimum() {
        // Arrange
        PayoutCalculator customCalculator = new PayoutCalculator(
                new BigDecimal("2.00"),
                new BigDecimal("0.20"),
                10,
                60
        );
        deliveryTime = pickupTime.plusMinutes(5);

        // Act
        BigDecimal result = customCalculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("4.00"), result);
    }

    @Test
    void calculateFor_WithDurationAboveMaximum_ShouldApplyMaximum() {
        // Arrange
        PayoutCalculator customCalculator = new PayoutCalculator(
                new BigDecimal("1.50"),
                new BigDecimal("0.25"),
                5,
                15
        );
        deliveryTime = pickupTime.plusMinutes(30);

        // Act
        BigDecimal result = customCalculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("5.25"), result);
    }

    @Test
    void calculateFor_ShouldAlwaysRoundToTwoDecimalsCeiling() {
        // Arrange
        PayoutCalculator fractionalCalculator = new PayoutCalculator(
                new BigDecimal("1.00"),
                new BigDecimal("0.333"),
                1,
                60
        );
        deliveryTime = pickupTime.plusMinutes(3);

        // Act
        BigDecimal result = fractionalCalculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("2.00"), result);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 4.50",
            "1, 4.50",
            "5, 4.50",
            "10, 6.00",
            "15, 7.50",
            "30, 12.00",
            "31, 12.00",
            "60, 12.00"
    })
    void calculateFor_WithVariousDurations_ShouldCalculateCorrectly(int minutes, String expected) {
        // Arrange
        deliveryTime = pickupTime.plusMinutes(minutes);

        // Act
        BigDecimal result = calculator.calculateFor(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal(expected), result);
    }

    @Test
    void record_ShouldHaveProperRecordBehavior() {
        // Arrange
        PayoutCalculator calculator1 = new PayoutCalculator(
                new BigDecimal("3.00"), new BigDecimal("0.30"), 5, 30);
        PayoutCalculator calculator2 = new PayoutCalculator(
                new BigDecimal("3.00"), new BigDecimal("0.30"), 5, 30);

        // Act & Assert
        assertEquals(calculator1, calculator2);
        assertEquals(calculator1.hashCode(), calculator2.hashCode());
        assertNotNull(calculator1.toString());

        assertEquals(new BigDecimal("3.00"), calculator1.baseFee());
        assertEquals(new BigDecimal("0.30"), calculator1.perMinuteRate());
        assertEquals(5, calculator1.minMinutes());
        assertEquals(30, calculator1.maxMinutes());
    }
}