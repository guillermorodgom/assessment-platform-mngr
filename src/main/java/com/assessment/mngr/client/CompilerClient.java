package com.assessment.mngr.client;

import com.assessment.mngr.controller.dto.CompilerRequest;
import com.assessment.mngr.controller.dto.CompilerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompilerClient {

    private final RestTemplate restTemplate;

    @Value("${compiler.api.url}")
    private String compilerApiUrl;

    public CompilerResponse execute(CompilerRequest request) {
        try {
            String url = compilerApiUrl + "/api/compiler/execute";
            return restTemplate.postForObject(url, request, CompilerResponse.class);
        } catch (RestClientException e) {
            log.error("Error al comunicarse con el servicio de compilacion: {}", e.getMessage());
            return new CompilerResponse(false, null, "Error de conexion con el servicio de compilacion: " + e.getMessage(), null);
        }
    }
}
