package com.assessment.mngr.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCuestionarioRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    String nombre,

    @Size(max = 2000, message = "La descripcion no puede exceder 2000 caracteres")
    String descripcion,

    @NotNull(message = "El tiempo limite es obligatorio")
    @Positive(message = "El tiempo limite debe ser positivo")
    Integer tiempoLimite,

    @NotNull(message = "El maximo de intentos es obligatorio")
    @Positive(message = "El maximo de intentos debe ser positivo")
    Integer maxIntentos
) {}
