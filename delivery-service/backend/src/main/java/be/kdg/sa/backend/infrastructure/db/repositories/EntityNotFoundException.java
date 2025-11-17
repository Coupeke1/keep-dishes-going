package be.kdg.sa.backend.infrastructure.db.repositories;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(Object o) {
        super("Entity not found: " + o.toString());
    }


}
