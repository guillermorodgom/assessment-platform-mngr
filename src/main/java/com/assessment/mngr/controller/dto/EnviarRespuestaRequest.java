package com.assessment.mngr.controller.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EnviarRespuestaRequest(
    @NotNull(message = "El id de la pregunta es obligatorio")
    Long preguntaId,

    String codigoFuente,

    String lenguaje,

    List<Long> opcionesSeleccionadas
) {}
