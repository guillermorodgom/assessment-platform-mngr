package com.assessment.mngr.controller.dto;

import com.assessment.mngr.model.EstadoIntento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ResultadoIntentoResponse(
    Long intentoId,
    Long candidatoId,
    String candidatoNombre,
    Long cuestionarioId,
    String cuestionarioNombre,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    EstadoIntento estado,
    BigDecimal puntajeTotal,
    BigDecimal puntajeMaximo,
    Integer tiempoConsumido,
    List<RespuestaCandidatoResponse> respuestas
) {}
