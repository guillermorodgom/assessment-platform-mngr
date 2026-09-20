package com.assessment.mngr.service;

import com.assessment.mngr.controller.dto.CreateCuestionarioRequest;
import com.assessment.mngr.controller.dto.CuestionarioResponse;
import com.assessment.mngr.controller.dto.UpdateCuestionarioRequest;
import com.assessment.mngr.exception.EntityNotFoundException;
import com.assessment.mngr.model.Cuestionario;
import com.assessment.mngr.model.Usuario;
import com.assessment.mngr.repository.CuestionarioRepository;
import com.assessment.mngr.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CuestionarioService {

    private final CuestionarioRepository cuestionarioRepository;
    private final UsuarioRepository usuarioRepository;

    public CuestionarioResponse crear(CreateCuestionarioRequest request, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("Usuario", username));

        Cuestionario cuestionario = Cuestionario.builder()
            .nombre(request.nombre())
            .descripcion(request.descripcion())
            .tiempoLimite(request.tiempoLimite())
            .maxIntentos(request.maxIntentos())
            .creadoPor(usuario)
            .build();

        cuestionario = cuestionarioRepository.save(cuestionario);
        log.info("[CREAR] Cuestionario creado — id: {}, nombre: {}, por: {}", cuestionario.getId(), cuestionario.getNombre(), username);
        return toResponse(cuestionario);
    }

    @Transactional(readOnly = true)
    public List<CuestionarioResponse> listarTodos() {
        return cuestionarioRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CuestionarioResponse> listarActivos() {
        return cuestionarioRepository.findByActivoTrue().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public CuestionarioResponse buscarPorId(Long id) {
        Cuestionario cuestionario = cuestionarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Cuestionario", id));
        return toResponse(cuestionario);
    }

    public CuestionarioResponse actualizar(Long id, UpdateCuestionarioRequest request) {
        Cuestionario cuestionario = cuestionarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Cuestionario", id));

        if (request.nombre() != null) cuestionario.setNombre(request.nombre());
        if (request.descripcion() != null) cuestionario.setDescripcion(request.descripcion());
        if (request.tiempoLimite() != null) cuestionario.setTiempoLimite(request.tiempoLimite());
        if (request.maxIntentos() != null) cuestionario.setMaxIntentos(request.maxIntentos());
        if (request.activo() != null) cuestionario.setActivo(request.activo());

        cuestionario = cuestionarioRepository.save(cuestionario);
        log.info("[ACTUALIZAR] Cuestionario actualizado — id: {}", id);
        return toResponse(cuestionario);
    }

    public void eliminar(Long id) {
        if (!cuestionarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Cuestionario", id);
        }
        cuestionarioRepository.deleteById(id);
        log.info("[ELIMINAR] Cuestionario eliminado — id: {}", id);
    }

    private CuestionarioResponse toResponse(Cuestionario c) {
        return new CuestionarioResponse(
            c.getId(),
            c.getNombre(),
            c.getDescripcion(),
            c.getTiempoLimite(),
            c.getCantidadPreguntas(),
            c.getMaxIntentos(),
            c.getActivo(),
            c.getCreadoPor().getUsername(),
            c.getCreatedAt()
        );
    }
}
