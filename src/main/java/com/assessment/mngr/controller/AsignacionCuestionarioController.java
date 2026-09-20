package com.assessment.mngr.controller;

import com.assessment.mngr.controller.dto.AsignacionBatchRequest;
import com.assessment.mngr.controller.dto.AsignacionResponse;
import com.assessment.mngr.controller.dto.CreateAsignacionRequest;
import com.assessment.mngr.service.AsignacionCuestionarioService;
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
@RequestMapping("/api/asignaciones")
@RequiredArgsConstructor
public class AsignacionCuestionarioController {

    private final AsignacionCuestionarioService asignacionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AsignacionResponse> crear(
            @Valid @RequestBody CreateAsignacionRequest request,
            Authentication authentication) {
        log.info("[REQUEST] POST /api/asignaciones — usuario: {}, cuestionarioId: {}, candidatoId: {}",
            authentication.getName(), request.cuestionarioId(), request.candidatoId());
        AsignacionResponse response = asignacionService.crear(request, authentication.getName());
        log.info("[RESPONSE] POST /api/asignaciones — id: {}, status: 201", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AsignacionResponse>> crearBatch(
            @Valid @RequestBody AsignacionBatchRequest request,
            Authentication authentication) {
        log.info("[REQUEST] POST /api/asignaciones/batch — usuario: {}, cuestionarioId: {}, candidatos: {}",
            authentication.getName(), request.cuestionarioId(), request.candidatoIds().size());
        List<AsignacionResponse> responses = asignacionService.crearBatch(request, authentication.getName());
        log.info("[RESPONSE] POST /api/asignaciones/batch — creados: {}", responses.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("[REQUEST] DELETE /api/asignaciones/{}", id);
        asignacionService.eliminar(id);
        log.info("[RESPONSE] DELETE /api/asignaciones/{} — status: 204", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cuestionario/{cuestionarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AsignacionResponse>> listarPorCuestionario(@PathVariable Long cuestionarioId) {
        log.info("[REQUEST] GET /api/asignaciones/cuestionario/{}", cuestionarioId);
        return ResponseEntity.ok(asignacionService.listarPorCuestionario(cuestionarioId));
    }

    @GetMapping("/mis-asignaciones")
    @PreAuthorize("hasRole('CANDIDATO')")
    public ResponseEntity<List<AsignacionResponse>> misAsignaciones(Authentication authentication) {
        log.info("[REQUEST] GET /api/asignaciones/mis-asignaciones — usuario: {}", authentication.getName());
        return ResponseEntity.ok(asignacionService.listarAsignacionesCandidato(authentication.getName()));
    }
}
