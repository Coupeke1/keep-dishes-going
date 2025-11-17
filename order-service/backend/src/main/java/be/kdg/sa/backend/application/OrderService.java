package be.kdg.sa.backend.application;

import be.kdg.sa.backend.domain.order.Order;
import be.kdg.sa.backend.domain.order.OrderId;
import be.kdg.sa.backend.domain.order.OrderRepository;
import be.kdg.sa.backend.domain.restaurant.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orders;
    private final RestaurantCatalog restaurants;

    public OrderService(final OrderRepository orders,
                        final RestaurantCatalog restaurants) {
        this.orders = orders;
        this.restaurants = restaurants;
    }

    public Order create() {
        final Order order = Order.createCart();
        orders.save(order);
        return order;
    }

    public Order findById(final OrderId orderId) {
        return orders.findById(orderId).orElseThrow(orderId::notFound);
    }

    public List<Order> findAll() {
        return orders.findAll();
    }

    public List<Order> findByDishId(final DishId dishId) {
        return orders.findByDishId(dishId);
    }

    public Order addDish(final OrderId orderId, final RestaurantId restaurantId, final DishId dishId, final int quantity, String notes) {
        final Order order = orders.findById(orderId).orElseThrow(orderId::notFound);
        final Dish dish = restaurants.getDish(restaurantId, dishId).orElseThrow(dishId::notFound);
        order.addDish(dishId, restaurantId, dish, quantity, notes);
        orders.save(order);
        return order;
    }

    public Order setCustomerDetails(
            final OrderId orderId,
            String name,
            String email,
            Address address
    ) {
        final Order order = orders.findById(orderId).orElseThrow(orderId::notFound);

        if (!restaurants.isRestaurantOpen(order.getRestaurantId())) {
            throw new IllegalStateException("Cannot place order, the restaurant is currently closed.");
        }

        order.getLines().forEach(line -> {
            Dish dish = restaurants.getDish(order.getRestaurantId(), line.getDishId())
                    .orElseThrow(() -> new IllegalStateException("Dish " + line.getDishName() + " no longer exists."));

            if (dish.status() == null || !dish.status().equalsIgnoreCase("AVAILABLE")) {
                throw new IllegalStateException("Dish " + dish.name() + " is not available for ordering.");
            }

            line.setUnitPrice(dish.price());
        });

        order.setCustomerDetails(name, email, address);
        
        orders.save(order);

        return order;
    }

}