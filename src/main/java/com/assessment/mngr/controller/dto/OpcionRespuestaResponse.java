package com.assessment.mngr.controller.dto;

public record OpcionRespuestaResponse(
    Long id,
    String texto,
    Boolean esCorrecta
) {}
