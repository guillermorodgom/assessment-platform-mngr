package com.assessment.mngr.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCasoDePruebaRequest(
    String input,

    @NotBlank(message = "El output esperado es obligatorio")
    String expectedOutput
) {}
