package com.assessment.mngr.controller.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateAsignacionRequest(
    @NotNull(message = "El cuestionarioId es obligatorio")
    Long cuestionarioId,

    @NotNull(message = "El candidatoId es obligatorio")
    Long candidatoId,

    @NotNull(message = "La fecha disponibleDesde es obligatoria")
    LocalDateTime disponibleDesde,

    @NotNull(message = "La fecha disponibleHasta es obligatoria")
    LocalDateTime disponibleHasta
) {}
