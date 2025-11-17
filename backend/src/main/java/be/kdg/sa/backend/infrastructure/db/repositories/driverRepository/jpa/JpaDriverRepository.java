package be.kdg.sa.backend.infrastructure.db.repositories.driverRepository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaDriverRepository extends JpaRepository<JpaDriverEntity, UUID> {


}
