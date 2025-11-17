package be.kdg.sa.backend.application.services.payment;

import be.kdg.sa.backend.config.PayoutProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayoutServiceTest {

    private PayoutService payoutService;

    @Mock
    private PayoutProperties payoutProperties;

    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;

    @BeforeEach
    void setUp() {
        when(payoutProperties.getBaseFee()).thenReturn(new BigDecimal("3.00"));
        when(payoutProperties.getPerMinute()).thenReturn(new BigDecimal("0.30"));
        when(payoutProperties.getMinMinutes()).thenReturn(5);
        when(payoutProperties.getMaxMinutes()).thenReturn(30);

        payoutService = new PayoutService(payoutProperties);
        pickupTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        deliveryTime = LocalDateTime.of(2024, 1, 1, 10, 15);
    }

    @Test
    void calculatePayout_WithValidTimes_ShouldReturnCorrectPayout() {
        // Act
        BigDecimal result = payoutService.calculatePayout(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("7.50"), result);
    }

    @Test
    void calculatePayout_WithNullPickupTime_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> payoutService.calculatePayout(null, deliveryTime));
        assertEquals("Tijden mogen niet null zijn", exception.getMessage());
    }

    @Test
    void calculatePayout_WithNullDeliveryTime_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> payoutService.calculatePayout(pickupTime, null));
        assertEquals("Tijden mogen niet null zijn", exception.getMessage());
    }

    @Test
    void calculatePayout_WithDeliveryTimeBeforePickupTime_ShouldThrowException() {
        // Arrange
        LocalDateTime earlierTime = pickupTime.minusMinutes(10);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> payoutService.calculatePayout(pickupTime, earlierTime));
        assertEquals("deliveryTime moet na pickupTime zijn", exception.getMessage());
    }

    @Test
    void calculatePayout_WithVeryShortDuration_ShouldApplyMinimumMinutes() {
        // Arrange
        deliveryTime = pickupTime.plusMinutes(2);

        // Act
        BigDecimal result = payoutService.calculatePayout(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("4.50"), result);
    }

    @Test
    void calculatePayout_WithVeryLongDuration_ShouldApplyMaximumMinutes() {
        // Arrange
        deliveryTime = pickupTime.plusMinutes(90);

        // Act
        BigDecimal result = payoutService.calculatePayout(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("12.00"), result);
    }

    @Test
    void calculatePayout_WithExactMinimumMinutes_ShouldCalculateCorrectly() {
        // Arrange
        deliveryTime = pickupTime.plusMinutes(5);

        // Act
        BigDecimal result = payoutService.calculatePayout(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("4.50"), result);
    }

    @Test
    void calculatePayout_WithExactMaximumMinutes_ShouldCalculateCorrectly() {
        // Arrange
        deliveryTime = pickupTime.plusMinutes(30);

        // Act
        BigDecimal result = payoutService.calculatePayout(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("12.00"), result);
    }

    @Test
    void calculatePayout_WithPartialMinutes_ShouldRoundUp() {
        // Arrange
        deliveryTime = pickupTime.plusMinutes(15).plusSeconds(30);

        // Act
        BigDecimal result = payoutService.calculatePayout(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("7.80"), result);
    }

    @Test
    void calculatePayout_WithZeroSeconds_ShouldNotRoundUp() {
        // Arrange
        deliveryTime = pickupTime.plusMinutes(15);

        // Act
        BigDecimal result = payoutService.calculatePayout(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("7.50"), result);
    }

    @Test
    void calculatePayout_WithOneSecond_ShouldRoundUpToOneMinute() {
        // Arrange
        deliveryTime = pickupTime.plusSeconds(1);

        // Act
        BigDecimal result = payoutService.calculatePayout(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("4.50"), result);
    }

    @Test
    void calculatePayout_With59Seconds_ShouldRoundUpToOneMinute() {
        // Arrange
        deliveryTime = pickupTime.plusSeconds(59);

        // Act
        BigDecimal result = payoutService.calculatePayout(pickupTime, deliveryTime);

        // Assert
        assertEquals(new BigDecimal("4.50"), result);
    }
}