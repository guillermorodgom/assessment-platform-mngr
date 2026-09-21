package com.assessment.mngr.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EjecutarCodigoRequest(
    @NotNull(message = "El id de la pregunta es obligatorio")
    Long preguntaId,

    @NotBlank(message = "El codigo fuente es obligatorio")
    String sourceCode,

    @NotBlank(message = "El lenguaje es obligatorio")
    String language
) {}
