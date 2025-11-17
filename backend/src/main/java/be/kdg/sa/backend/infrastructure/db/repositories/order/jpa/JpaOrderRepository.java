package be.kdg.sa.backend.infrastructure.db.repositories.order.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaOrderRepository extends JpaRepository<JpaOrderEntity, UUID> {
    @Query(value = """
                    select o
                    from JpaOrderEntity o
                    join o.lines l
                    where l.id.dishId = :dishId
            """)
    List<JpaOrderEntity> findAllByDishId(UUID dishId);

    @Query(value = """
                    select o
                    from JpaOrderEntity o
                    where o.payment.id = :paymentId
            """)
    Optional<JpaOrderEntity> findByPaymentId(String paymentId);
}
