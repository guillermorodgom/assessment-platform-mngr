package com.assessment.mngr.controller.dto;

import com.assessment.mngr.model.LenguajeProgramacion;
import com.assessment.mngr.model.TipoPregunta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreatePreguntaRequest(
    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 300, message = "El titulo no puede exceder 300 caracteres")
    String titulo,

    String descripcion,

    @NotNull(message = "El tipo de pregunta es obligatorio")
    TipoPregunta tipoPregunta,

    List<LenguajeProgramacion> lenguajesPermitidos,

    @Positive(message = "El puntaje debe ser positivo")
    BigDecimal puntaje
) {}
