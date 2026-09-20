package com.assessment.mngr.controller;

import com.assessment.mngr.controller.dto.ResultadoIntentoResponse;
import com.assessment.mngr.service.IntentoExamenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/resultados")
@RequiredArgsConstructor
public class ResultadoController {

    private final IntentoExamenService intentoExamenService;

    @GetMapping("/cuestionario/{cuestionarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ResultadoIntentoResponse>> resultadosPorCuestionario(
            @PathVariable Long cuestionarioId) {
        log.info("[REQUEST] GET /api/resultados/cuestionario/{}", cuestionarioId);
        return ResponseEntity.ok(intentoExamenService.listarResultadosPorCuestionario(cuestionarioId));
    }

    @GetMapping("/intento/{intentoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResultadoIntentoResponse> resultadoDetallado(
            @PathVariable Long intentoId) {
        log.info("[REQUEST] GET /api/resultados/intento/{}", intentoId);
        return ResponseEntity.ok(intentoExamenService.obtenerResultadoDetallado(intentoId));
    }
}
