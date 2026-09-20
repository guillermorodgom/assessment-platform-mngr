package com.assessment.mngr.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex) {
        log.warn("[NOT_FOUND] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiError(404, "NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateEntityException ex) {
        log.warn("[DUPLICATE] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiError(409, "DUPLICATE", ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        log.warn("[NEGOCIO] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiError(400, "BUSINESS_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        log.warn("[AUTH] Acceso denegado — {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ApiError(403, "FORBIDDEN", "No tiene permisos para realizar esta accion"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        log.warn("[VALIDACION] Errores: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError.builder()
                .status(400)
                .code("VALIDATION_ERROR")
                .message("Errores de validacion")
                .details(errors)
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex) {
        log.error("[ERROR] Error interno del servidor", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiError(500, "INTERNAL_ERROR", "Error interno del servidor"));
    }
}
