package com.assessment.mngr.controller.dto;

import jakarta.validation.constraints.NotNull;

public record IniciarIntentoRequest(
    @NotNull(message = "El id del cuestionario es obligatorio")
    Long cuestionarioId
) {}
