package be.kdg.sa.backend.domain.order;

import be.kdg.sa.backend.domain.restaurant.DishId;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.util.Assert;

import java.math.BigDecimal;

@Entity
@Getter
public class OrderLine {
    @Identity
    private final DishId dishId;
    private final String dishName;
    @Setter
    private BigDecimal unitPrice;
    private int quantity;
    @Setter
    private String notes;

    public OrderLine(final DishId dishId, String dishName, final BigDecimal unitPrice, final int quantity, final String notes) {
        Assert.notNull(dishId, "DishId must not be null");
        validateQuantityMustBeLargerThanZero(quantity);
        validatePriceNotNegatie(unitPrice);

        this.dishId = dishId;
        this.dishName = dishName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.notes = notes;
    }

    public void addQuantity(final int quantity) {
        validateQuantityMustBeLargerThanZero(quantity);
        this.quantity += quantity;
    }

    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean isForDish(DishId dishId) {
        return this.dishId.equals(dishId);
    }

    private static void validateQuantityMustBeLargerThanZero(final int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
    }

    private static void validatePriceNotNegatie(final BigDecimal unitPrice) {
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
    }


}
