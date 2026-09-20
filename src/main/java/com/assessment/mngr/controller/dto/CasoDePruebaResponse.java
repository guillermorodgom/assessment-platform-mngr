package com.assessment.mngr.controller.dto;

public record CasoDePruebaResponse(
    Long id,
    String input,
    String expectedOutput
) {}
