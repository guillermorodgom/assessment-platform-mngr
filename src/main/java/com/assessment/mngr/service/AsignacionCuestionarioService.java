package com.assessment.mngr.service;

import com.assessment.mngr.controller.dto.AsignacionBatchRequest;
import com.assessment.mngr.controller.dto.AsignacionResponse;
import com.assessment.mngr.controller.dto.CreateAsignacionRequest;
import com.assessment.mngr.exception.BusinessException;
import com.assessment.mngr.exception.DuplicateEntityException;
import com.assessment.mngr.exception.EntityNotFoundException;
import com.assessment.mngr.model.AsignacionCuestionario;
import com.assessment.mngr.model.Cuestionario;
import com.assessment.mngr.model.EstadoIntento;
import com.assessment.mngr.model.Usuario;
import com.assessment.mngr.repository.AsignacionCuestionarioRepository;
import com.assessment.mngr.repository.CuestionarioRepository;
import com.assessment.mngr.repository.IntentoExamenRepository;
import com.assessment.mngr.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AsignacionCuestionarioService {

    private final AsignacionCuestionarioRepository asignacionRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final IntentoExamenRepository intentoExamenRepository;

    public AsignacionResponse crear(CreateAsignacionRequest request, String username) {
        if (!request.disponibleHasta().isAfter(request.disponibleDesde())) {
            throw new BusinessException("La fecha 'disponibleHasta' debe ser posterior a 'disponibleDesde'");
        }

        Cuestionario cuestionario = cuestionarioRepository.findById(request.cuestionarioId())
            .orElseThrow(() -> new EntityNotFoundException("Cuestionario", request.cuestionarioId()));

        Usuario candidato = usuarioRepository.findById(request.candidatoId())
            .orElseThrow(() -> new EntityNotFoundException("Usuario", request.candidatoId()));

        if (asignacionRepository.existsByCuestionarioIdAndCandidatoId(request.cuestionarioId(), request.candidatoId())) {
            throw new DuplicateEntityException("Ya existe una asignacion para este candidato y cuestionario");
        }

        Usuario admin = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("Usuario", username));

        AsignacionCuestionario asignacion = AsignacionCuestionario.builder()
            .cuestionario(cuestionario)
            .candidato(candidato)
            .disponibleDesde(request.disponibleDesde())
            .disponibleHasta(request.disponibleHasta())
            .asignadoPor(admin)
            .build();

        asignacion = asignacionRepository.save(asignacion);
        log.info("[CREAR] Asignacion creada — id: {}, cuestionarioId: {}, candidatoId: {}, por: {}",
            asignacion.getId(), request.cuestionarioId(), request.candidatoId(), username);
        return toResponse(asignacion);
    }

    public List<AsignacionResponse> crearBatch(AsignacionBatchRequest request, String username) {
        if (!request.disponibleHasta().isAfter(request.disponibleDesde())) {
            throw new BusinessException("La fecha 'disponibleHasta' debe ser posterior a 'disponibleDesde'");
        }

        List<AsignacionResponse> resultados = new ArrayList<>();
        int creados = 0;
        int duplicados = 0;

        for (Long candidatoId : request.candidatoIds()) {
            try {
                CreateAsignacionRequest individual = new CreateAsignacionRequest(
                    request.cuestionarioId(), candidatoId, request.disponibleDesde(), request.disponibleHasta()
                );
                resultados.add(crear(individual, username));
                creados++;
            } catch (DuplicateEntityException e) {
                duplicados++;
                log.warn("[NEGOCIO] Asignacion duplicada omitida — cuestionarioId: {}, candidatoId: {}", request.cuestionarioId(), candidatoId);
            }
        }

        log.info("[CREAR] Asignacion batch — cuestionarioId: {}, creados: {}, duplicados: {}, total: {}",
            request.cuestionarioId(), creados, duplicados, request.candidatoIds().size());
        return resultados;
    }

    public void eliminar(Long id) {
        AsignacionCuestionario asignacion = asignacionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("AsignacionCuestionario", id));
        asignacionRepository.delete(asignacion);
        log.info("[ELIMINAR] Asignacion eliminada — id: {}, cuestionarioId: {}, candidatoId: {}",
            id, asignacion.getCuestionario().getId(), asignacion.getCandidato().getId());
    }

    @Transactional(readOnly = true)
    public List<AsignacionResponse> listarPorCuestionario(Long cuestionarioId) {
        if (!cuestionarioRepository.existsById(cuestionarioId)) {
            throw new EntityNotFoundException("Cuestionario", cuestionarioId);
        }
        return asignacionRepository.findByCuestionarioId(cuestionarioId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<AsignacionResponse> listarAsignacionesCandidato(String username) {
        Usuario candidato = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("Usuario", username));

        return asignacionRepository.findByCandidatoId(candidato.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    public void validarAsignacionActiva(Long candidatoId, Long cuestionarioId) {
        AsignacionCuestionario asignacion = asignacionRepository
            .findByCuestionarioIdAndCandidatoId(cuestionarioId, candidatoId)
            .orElseThrow(() -> new BusinessException("No tiene una asignacion activa para este cuestionario"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(asignacion.getDisponibleDesde())) {
            throw new BusinessException("La evaluacion aun no esta disponible. Disponible desde: " + asignacion.getDisponibleDesde());
        }
        if (now.isAfter(asignacion.getDisponibleHasta())) {
            throw new BusinessException("La ventana de disponibilidad ha expirado. Disponible hasta: " + asignacion.getDisponibleHasta());
        }
    }

    private String calcularEstado(AsignacionCuestionario a) {
        LocalDateTime now = LocalDateTime.now();
        if (!a.getCuestionario().getActivo()) {
            return "INACTIVA";
        }
        if (now.isBefore(a.getDisponibleDesde())) {
            return "PENDIENTE";
        }
        if (now.isAfter(a.getDisponibleHasta())) {
            return "EXPIRADA";
        }
        return "ACTIVA";
    }

    private AsignacionResponse toResponse(AsignacionCuestionario a) {
        int intentosUsados = (int) intentoExamenRepository
            .findByCandidatoIdAndCuestionarioId(a.getCandidato().getId(), a.getCuestionario().getId())
            .stream()
            .filter(i -> i.getEstado() == EstadoIntento.FINALIZADO || i.getEstado() == EstadoIntento.ABANDONADO)
            .count();

        return new AsignacionResponse(
            a.getId(),
            a.getCuestionario().getId(),
            a.getCuestionario().getNombre(),
            a.getCandidato().getId(),
            a.getCandidato().getNombreCompleto(),
            a.getCandidato().getEmail(),
            a.getAsignadoPor().getNombreCompleto(),
            a.getDisponibleDesde(),
            a.getDisponibleHasta(),
            a.getCreatedAt(),
            calcularEstado(a),
            a.getCuestionario().getTiempoLimite(),
            a.getCuestionario().getMaxIntentos(),
            intentosUsados
        );
    }
}
