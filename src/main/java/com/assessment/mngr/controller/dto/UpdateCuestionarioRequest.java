package com.assessment.mngr.controller.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateCuestionarioRequest(
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    String nombre,

    @Size(max = 2000, message = "La descripcion no puede exceder 2000 caracteres")
    String descripcion,

    @Positive(message = "El tiempo limite debe ser positivo")
    Integer tiempoLimite,

    @Positive(message = "El maximo de intentos debe ser positivo")
    Integer maxIntentos,

    Boolean activo
) {}
