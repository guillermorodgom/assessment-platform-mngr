package com.assessment.mngr.controller.dto;

import java.util.List;

public record CompilerRequest(
    String sourceCode,
    String language,
    List<TestCaseDto> testCases
) {
    public record TestCaseDto(
        String input,
        String expectedOutput
    ) {}
}
