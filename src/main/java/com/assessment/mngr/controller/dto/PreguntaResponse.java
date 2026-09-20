package com.assessment.mngr.controller.dto;

import com.assessment.mngr.model.LenguajeProgramacion;
import com.assessment.mngr.model.TipoPregunta;

import java.math.BigDecimal;
import java.util.List;

public record PreguntaResponse(
    Long id,
    String titulo,
    String descripcion,
    TipoPregunta tipoPregunta,
    List<LenguajeProgramacion> lenguajesPermitidos,
    BigDecimal puntaje,
    List<CuestionarioSimpleResponse> cuestionarios,
    List<OpcionRespuestaResponse> opciones,
    List<CasoDePruebaResponse> casosDePrueba
) {}
