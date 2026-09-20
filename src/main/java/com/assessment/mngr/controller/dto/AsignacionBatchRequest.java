package com.assessment.mngr.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record AsignacionBatchRequest(
    @NotNull(message = "El cuestionarioId es obligatorio")
    Long cuestionarioId,

    @NotEmpty(message = "Debe incluir al menos un candidatoId")
    List<Long> candidatoIds,

    @NotNull(message = "La fecha disponibleDesde es obligatoria")
    LocalDateTime disponibleDesde,

    @NotNull(message = "La fecha disponibleHasta es obligatoria")
    LocalDateTime disponibleHasta
) {}
