package be.kdg.sa.backend.infrastructure.db.repositories.driverRepository;

import be.kdg.sa.backend.domain.driver.Driver;
import be.kdg.sa.backend.domain.driver.DriverId;
import be.kdg.sa.backend.infrastructure.db.repositories.EntityNotFoundException;
import be.kdg.sa.backend.infrastructure.db.repositories.driverRepository.jpa.JpaDriverEntity;
import be.kdg.sa.backend.infrastructure.db.repositories.driverRepository.jpa.JpaDriverRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class DbDriver implements DriverRepository {
    private final JpaDriverRepository jpaDriverRepository;

    public DbDriver(JpaDriverRepository jpaDriverRepository) {
        this.jpaDriverRepository = Objects.requireNonNull(jpaDriverRepository, "jpaDriverRepository must not be null");
    }

    @Override
    public void save(Driver driver) {
        if (driver == null) throw new IllegalArgumentException("Driver must not be null.");
        JpaDriverEntity driverEntity = JpaDriverEntity.fromDomain(driver);
        jpaDriverRepository.save(driverEntity);
    }

    @Override
    public Driver findById(DriverId driverId) {
        return jpaDriverRepository.findById(driverId.id())
                .map(JpaDriverEntity::toDomain)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found: " + driverId.id()));
    }

    @Override
    public List<Driver> findAll() {
        return jpaDriverRepository.findAll().stream()
                .map(JpaDriverEntity::toDomain)
                .toList();
    }

}
