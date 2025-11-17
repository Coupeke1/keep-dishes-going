package be.kdg.sa.backend.infrastructure.db.repositories.order;


import be.kdg.sa.backend.domain.restaurant.DishId;
import be.kdg.sa.backend.domain.order.Order;
import be.kdg.sa.backend.domain.order.OrderId;
import be.kdg.sa.backend.domain.order.OrderRepository;
import be.kdg.sa.backend.infrastructure.db.repositories.order.jpa.JpaOrderEntity;
import be.kdg.sa.backend.infrastructure.db.repositories.order.jpa.JpaOrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DbOrderRepository implements OrderRepository {
    private final JpaOrderRepository jpaOrderRepository;

    DbOrderRepository(JpaOrderRepository jpaOrderRepository) {
        this.jpaOrderRepository = jpaOrderRepository;
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return this.jpaOrderRepository.findById(orderId.value())
                .map(JpaOrderEntity::toDomain);
    }

    @Override
    public void save(Order order) {
        JpaOrderEntity jpaOrderEntity = JpaOrderEntity.fromDomain(order);
        this.jpaOrderRepository.save(jpaOrderEntity);
    }

    @Override
    public List<Order> findAll() {
        return this.jpaOrderRepository.findAll().stream()
                .map(JpaOrderEntity::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByDishId(DishId dishId) {
        return this.jpaOrderRepository.findAllByDishId(dishId.value()).stream()
                .map(JpaOrderEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Order> findByPaymentId(String molliePaymentId) {
        return this.jpaOrderRepository.findByPaymentId(molliePaymentId)
                .map(JpaOrderEntity::toDomain);
    }
}
