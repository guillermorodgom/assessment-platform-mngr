package com.assessment.mngr.controller.dto;

import com.assessment.mngr.model.ResultadoEjecucion;

import java.math.BigDecimal;

public record RespuestaCandidatoResponse(
    Long id,
    Long preguntaId,
    String preguntaTitulo,
    String codigoFuente,
    String lenguaje,
    String opcionesSeleccionadas,
    ResultadoEjecucion resultadoEjecucion,
    String salidaObtenida,
    Boolean esCorrecta,
    BigDecimal puntajeObtenido
) {}
