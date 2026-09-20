package com.assessment.mngr.controller;

import com.assessment.mngr.controller.dto.CreateCuestionarioRequest;
import com.assessment.mngr.controller.dto.CuestionarioResponse;
import com.assessment.mngr.controller.dto.UpdateCuestionarioRequest;
import com.assessment.mngr.service.CuestionarioService;
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
@RequestMapping("/api/cuestionarios")
@RequiredArgsConstructor
public class CuestionarioController {

    private final CuestionarioService cuestionarioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CuestionarioResponse> crear(
            @Valid @RequestBody CreateCuestionarioRequest request,
            Authentication authentication) {
        log.info("[REQUEST] POST /api/cuestionarios — usuario: {}", authentication.getName());
        CuestionarioResponse response = cuestionarioService.crear(request, authentication.getName());
        log.info("[RESPONSE] POST /api/cuestionarios — id: {}, status: 201", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CuestionarioResponse>> listarTodos() {
        log.info("[REQUEST] GET /api/cuestionarios");
        return ResponseEntity.ok(cuestionarioService.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<CuestionarioResponse>> listarActivos() {
        log.info("[REQUEST] GET /api/cuestionarios/activos");
        return ResponseEntity.ok(cuestionarioService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuestionarioResponse> buscarPorId(@PathVariable Long id) {
        log.info("[REQUEST] GET /api/cuestionarios/{}", id);
        return ResponseEntity.ok(cuestionarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CuestionarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCuestionarioRequest request) {
        log.info("[REQUEST] PUT /api/cuestionarios/{}", id);
        CuestionarioResponse response = cuestionarioService.actualizar(id, request);
        log.info("[RESPONSE] PUT /api/cuestionarios/{} — status: 200", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("[REQUEST] DELETE /api/cuestionarios/{}", id);
        cuestionarioService.eliminar(id);
        log.info("[RESPONSE] DELETE /api/cuestionarios/{} — status: 204", id);
        return ResponseEntity.noContent().build();
    }
}
