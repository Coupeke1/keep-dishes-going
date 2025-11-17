package be.kdg.sa.backend.domain.order;

import be.kdg.sa.backend.domain.restaurant.DishId;
import org.jmolecules.ddd.annotation.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository {
    Optional<Order> findById(OrderId orderId);

    void save(Order order);

    List<Order> findAll();

    List<Order> findByDishId(DishId dishId);
    Optional<Order> findByPaymentId(String molliePaymentId);
}
