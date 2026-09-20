package com.assessment.mngr.controller;

import com.assessment.mngr.controller.dto.*;
import com.assessment.mngr.service.IntentoExamenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/intentos")
@RequiredArgsConstructor
public class IntentoExamenController {

    private final IntentoExamenService intentoExamenService;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<IntentoExamenResponse> iniciarIntento(
            @Valid @RequestBody IniciarIntentoRequest request,
            Authentication authentication) {
        log.info("[REQUEST] POST /api/intentos — usuario: {}, cuestionarioId: {}", authentication.getName(), request.cuestionarioId());
        IntentoExamenResponse response = intentoExamenService.iniciarIntento(request, authentication.getName());
        log.info("[RESPONSE] POST /api/intentos — intentoId: {}, status: 201", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{intentoId}/respuestas")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<RespuestaCandidatoResponse> enviarRespuesta(
            @PathVariable Long intentoId,
            @Valid @RequestBody EnviarRespuestaRequest request,
            Authentication authentication) {
        log.info("[REQUEST] POST /api/intentos/{}/respuestas — usuario: {}, preguntaId: {}", intentoId, authentication.getName(), request.preguntaId());
        RespuestaCandidatoResponse response = intentoExamenService.enviarRespuesta(intentoId, request, authentication.getName());
        log.info("[RESPONSE] POST /api/intentos/{}/respuestas — respuestaId: {}", intentoId, response.id());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{intentoId}/finalizar")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<ResultadoIntentoResponse> finalizarIntento(
            @PathVariable Long intentoId,
            Authentication authentication) {
        log.info("[REQUEST] POST /api/intentos/{}/finalizar — usuario: {}", intentoId, authentication.getName());
        ResultadoIntentoResponse response = intentoExamenService.finalizarIntento(intentoId, authentication.getName());
        log.info("[RESPONSE] POST /api/intentos/{}/finalizar — puntaje: {}", intentoId, response.puntajeTotal());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{intentoId}/abandonar")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<Void> abandonarIntento(
            @PathVariable Long intentoId,
            Authentication authentication) {
        log.info("[REQUEST] POST /api/intentos/{}/abandonar — usuario: {}", intentoId, authentication.getName());
        intentoExamenService.abandonarIntento(intentoId, authentication.getName());
        log.info("[RESPONSE] POST /api/intentos/{}/abandonar — status: 204", intentoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{intentoId}/resultado")
    @PreAuthorize("hasAnyRole('CANDIDATO', 'ADMIN')")
    public ResponseEntity<ResultadoIntentoResponse> getResultado(
            @PathVariable Long intentoId,
            Authentication authentication) {
        log.info("[REQUEST] GET /api/intentos/{}/resultado — usuario: {}", intentoId, authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        ResultadoIntentoResponse response;
        if (isAdmin) {
            response = intentoExamenService.obtenerResultadoDetallado(intentoId);
        } else {
            response = intentoExamenService.obtenerResultadoCandidato(intentoId, authentication.getName());
        }
        log.info("[RESPONSE] GET /api/intentos/{}/resultado — estado: {}", intentoId, response.estado());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mis-intentos")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<List<IntentoExamenResponse>> misIntentos(Authentication authentication) {
        log.info("[REQUEST] GET /api/intentos/mis-intentos — usuario: {}", authentication.getName());
        return ResponseEntity.ok(intentoExamenService.listarPorCandidato(authentication.getName()));
    }

    @GetMapping("/candidato/{candidatoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<IntentoExamenResponse>> listarPorCandidato(
            @PathVariable Long candidatoId,
            Authentication authentication) {
        log.info("[REQUEST] GET /api/intentos/candidato/{} — usuario: {}", candidatoId, authentication.getName());
        List<IntentoExamenResponse> intentos = intentoExamenService.listarPorCandidatoId(candidatoId);
        log.info("[RESPONSE] GET /api/intentos/candidato/{} — count: {}", candidatoId, intentos.size());
        return ResponseEntity.ok(intentos);
    }

    @GetMapping("/{intentoId}/preguntas")
    @PreAuthorize("hasAnyRole('CANDIDATO', 'ADMIN')")
    public ResponseEntity<List<PreguntaResponse>> getPreguntasDelIntento(
            @PathVariable Long intentoId,
            Authentication authentication) {
        log.info("[REQUEST] GET /api/intentos/{}/preguntas — usuario: {}", intentoId, authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<PreguntaResponse> preguntas = intentoExamenService.obtenerPreguntasDelIntento(
            intentoId, authentication.getName(), isAdmin);
        log.info("[RESPONSE] GET /api/intentos/{}/preguntas — count: {}", intentoId, preguntas.size());
        return ResponseEntity.ok(preguntas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CANDIDATO', 'ADMIN')")
    public ResponseEntity<IntentoExamenResponse> buscarPorId(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("[REQUEST] GET /api/intentos/{} — usuario: {}", id, authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(intentoExamenService.buscarPorIdConValidacion(id, authentication.getName(), isAdmin));
    }
}
