package com.assessment.mngr.controller;

import com.assessment.mngr.controller.dto.UsuarioResponse;
import com.assessment.mngr.controller.dto.UsuarioSimpleResponse;
import com.assessment.mngr.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listarTodos() {
        log.info("[REQUEST] GET /api/usuarios");
        List<UsuarioResponse> usuarios = usuarioRepository.findAll().stream()
            .map(u -> new UsuarioResponse(u.getId(), u.getUsername(), u.getNombreCompleto(), u.getEmail(), u.getRoles()))
            .toList();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/candidatos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioSimpleResponse>> listarCandidatos() {
        log.info("[REQUEST] GET /api/usuarios/candidatos");
        List<UsuarioSimpleResponse> candidatos = usuarioRepository.findCandidatos().stream()
            .map(u -> new UsuarioSimpleResponse(u.getId(), u.getUsername(), u.getNombreCompleto(), u.getEmail()))
            .toList();
        return ResponseEntity.ok(candidatos);
    }
}
