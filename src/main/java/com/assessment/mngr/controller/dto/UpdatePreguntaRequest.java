package com.assessment.mngr.controller.dto;

import com.assessment.mngr.model.LenguajeProgramacion;
import com.assessment.mngr.model.TipoPregunta;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UpdatePreguntaRequest(
    @Size(max = 300, message = "El titulo no puede exceder 300 caracteres")
    String titulo,

    String descripcion,

    TipoPregunta tipoPregunta,

    List<LenguajeProgramacion> lenguajesPermitidos,

    @Positive(message = "El puntaje debe ser positivo")
    BigDecimal puntaje
) {}
