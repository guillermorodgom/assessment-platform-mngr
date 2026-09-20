package com.assessment.mngr.controller.dto;

import java.util.Set;

public record UsuarioResponse(
    Long id,
    String username,
    String nombreCompleto,
    String email,
    Set<String> roles
) {}
