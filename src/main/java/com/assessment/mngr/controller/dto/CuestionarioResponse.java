package com.assessment.mngr.controller.dto;

import java.time.LocalDateTime;

public record CuestionarioResponse(
    Long id,
    String nombre,
    String descripcion,
    Integer tiempoLimite,
    Integer cantidadPreguntas,
    Integer maxIntentos,
    Boolean activo,
    String creadoPor,
    LocalDateTime createdAt
) {}
