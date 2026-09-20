package com.assessment.mngr.controller.dto;

import com.assessment.mngr.model.EstadoIntento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IntentoExamenResponse(
    Long id,
    Long candidatoId,
    String candidatoNombre,
    Long cuestionarioId,
    String cuestionarioNombre,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    EstadoIntento estado,
    BigDecimal puntajeTotal,
    BigDecimal puntajeMaximo,
    Integer tiempoConsumido
) {}
