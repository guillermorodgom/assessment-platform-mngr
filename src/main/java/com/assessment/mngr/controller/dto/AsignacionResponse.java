package com.assessment.mngr.controller.dto;

import java.time.LocalDateTime;

public record AsignacionResponse(
    Long id,
    Long cuestionarioId,
    String cuestionarioNombre,
    Long candidatoId,
    String candidatoNombre,
    String candidatoEmail,
    String asignadoPorNombre,
    LocalDateTime disponibleDesde,
    LocalDateTime disponibleHasta,
    LocalDateTime createdAt,
    String estado,
    Integer tiempoLimite,
    Integer maxIntentos,
    Integer intentosUsados
) {}
