package com.assessment.mngr.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOpcionRequest(
    @NotBlank(message = "El texto de la opcion es obligatorio")
    @Size(max = 500, message = "El texto no puede exceder 500 caracteres")
    String texto,

    Boolean esCorrecta
) {}
