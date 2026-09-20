package com.assessment.mngr.controller.dto;

public record UsuarioSimpleResponse(
    Long id,
    String username,
    String nombreCompleto,
    String email
) {}
