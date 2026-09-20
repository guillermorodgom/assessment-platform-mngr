package com.assessment.mngr.exception;

public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException(String entity, Object id) {
        super(entity + " no encontrado con id: " + id);
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
