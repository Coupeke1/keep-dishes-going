package be.kdg.sa.backend.infrastructure.db.repositories.driverRepository;

import be.kdg.sa.backend.domain.driver.Driver;
import be.kdg.sa.backend.domain.driver.DriverId;

import java.util.List;

public interface DriverRepository {


    void save(Driver driver);

    Driver findById(DriverId driverId);

    List<Driver> findAll();
}
