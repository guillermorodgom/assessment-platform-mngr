package com.assessment.mngr.controller;

import com.assessment.mngr.controller.dto.*;
import com.assessment.mngr.service.PreguntaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PreguntaController {

    private final PreguntaService preguntaService;

    @GetMapping("/api/preguntas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PreguntaResponse>> listarTodas() {
        log.info("[REQUEST] GET /api/preguntas");
        return ResponseEntity.ok(preguntaService.listarTodas());
    }

    @PostMapping("/api/preguntas/{id}/duplicar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PreguntaResponse> duplicar(
            @PathVariable Long id,
            @RequestParam Long cuestionarioId) {
        log.info("[REQUEST] POST /api/preguntas/{}/duplicar — cuestionarioDestinoId: {}", id, cuestionarioId);
        PreguntaResponse response = preguntaService.duplicar(id, cuestionarioId);
        log.info("[RESPONSE] POST /api/preguntas/{}/duplicar — copiaId: {}, status: 201", id, response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/cuestionarios/{cuestionarioId}/preguntas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PreguntaResponse> crear(
            @PathVariable Long cuestionarioId,
            @Valid @RequestBody CreatePreguntaRequest request) {
        log.info("[REQUEST] POST /api/cuestionarios/{}/preguntas", cuestionarioId);
        PreguntaResponse response = preguntaService.crear(cuestionarioId, request);
        log.info("[RESPONSE] POST /api/cuestionarios/{}/preguntas — preguntaId: {}, status: 201", cuestionarioId, response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/cuestionarios/{cuestionarioId}/preguntas")
    public ResponseEntity<List<PreguntaResponse>> listarPorCuestionario(
            @PathVariable Long cuestionarioId) {
        log.info("[REQUEST] GET /api/cuestionarios/{}/preguntas", cuestionarioId);
        return ResponseEntity.ok(preguntaService.listarPorCuestionario(cuestionarioId));
    }

    @GetMapping("/api/preguntas/{id}")
    public ResponseEntity<PreguntaResponse> buscarPorId(@PathVariable Long id) {
        log.info("[REQUEST] GET /api/preguntas/{}", id);
        return ResponseEntity.ok(preguntaService.buscarPorId(id));
    }

    @PutMapping("/api/preguntas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PreguntaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePreguntaRequest request) {
        log.info("[REQUEST] PUT /api/preguntas/{}", id);
        PreguntaResponse response = preguntaService.actualizar(id, request);
        log.info("[RESPONSE] PUT /api/preguntas/{} — status: 200", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/preguntas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("[REQUEST] DELETE /api/preguntas/{}", id);
        preguntaService.eliminar(id);
        log.info("[RESPONSE] DELETE /api/preguntas/{} — status: 204", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/cuestionarios/{cuestionarioId}/preguntas/{preguntaId}/asociar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PreguntaResponse> asociar(
            @PathVariable Long cuestionarioId, @PathVariable Long preguntaId) {
        log.info("[REQUEST] POST /api/cuestionarios/{}/preguntas/{}/asociar", cuestionarioId, preguntaId);
        PreguntaResponse response = preguntaService.asociar(preguntaId, cuestionarioId);
        log.info("[RESPONSE] POST asociar — preguntaId: {}, cuestionarioId: {}, status: 200", preguntaId, cuestionarioId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/cuestionarios/{cuestionarioId}/preguntas/{preguntaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desvincular(
            @PathVariable Long cuestionarioId, @PathVariable Long preguntaId) {
        log.info("[REQUEST] DELETE /api/cuestionarios/{}/preguntas/{}", cuestionarioId, preguntaId);
        preguntaService.desvincular(preguntaId, cuestionarioId);
        log.info("[RESPONSE] DELETE desvincular — preguntaId: {}, cuestionarioId: {}, status: 204", preguntaId, cuestionarioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/preguntas/{preguntaId}/opciones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OpcionRespuestaResponse> agregarOpcion(
            @PathVariable Long preguntaId,
            @Valid @RequestBody CreateOpcionRequest request) {
        log.info("[REQUEST] POST /api/preguntas/{}/opciones", preguntaId);
        OpcionRespuestaResponse response = preguntaService.agregarOpcion(preguntaId, request);
        log.info("[RESPONSE] POST /api/preguntas/{}/opciones — opcionId: {}, status: 201", preguntaId, response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/opciones/{opcionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarOpcion(@PathVariable Long opcionId) {
        log.info("[REQUEST] DELETE /api/opciones/{}", opcionId);
        preguntaService.eliminarOpcion(opcionId);
        log.info("[RESPONSE] DELETE /api/opciones/{} — status: 204", opcionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/preguntas/{preguntaId}/casos-de-prueba")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CasoDePruebaResponse> agregarCasoDePrueba(
            @PathVariable Long preguntaId,
            @Valid @RequestBody CreateCasoDePruebaRequest request) {
        log.info("[REQUEST] POST /api/preguntas/{}/casos-de-prueba", preguntaId);
        CasoDePruebaResponse response = preguntaService.agregarCasoDePrueba(preguntaId, request);
        log.info("[RESPONSE] POST /api/preguntas/{}/casos-de-prueba — casoId: {}, status: 201", preguntaId, response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/casos-de-prueba/{casoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarCasoDePrueba(@PathVariable Long casoId) {
        log.info("[REQUEST] DELETE /api/casos-de-prueba/{}", casoId);
        preguntaService.eliminarCasoDePrueba(casoId);
        log.info("[RESPONSE] DELETE /api/casos-de-prueba/{} — status: 204", casoId);
        return ResponseEntity.noContent().build();
    }
}
