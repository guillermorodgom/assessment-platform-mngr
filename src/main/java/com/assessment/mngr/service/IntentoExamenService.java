package com.assessment.mngr.service;

import com.assessment.mngr.controller.dto.*;
import com.assessment.mngr.exception.BusinessException;
import com.assessment.mngr.exception.EntityNotFoundException;
import com.assessment.mngr.model.*;
import com.assessment.mngr.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class IntentoExamenService {

    private final IntentoExamenRepository intentoExamenRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PreguntaRepository preguntaRepository;
    private final RespuestaCandidatoRepository respuestaCandidatoRepository;
    private final CalificacionService calificacionService;
    private final AsignacionCuestionarioService asignacionCuestionarioService;
    private final PreguntaService preguntaService;

    public IntentoExamenResponse iniciarIntento(IniciarIntentoRequest request, String username) {
        Usuario candidato = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("Usuario", username));

        Cuestionario cuestionario = cuestionarioRepository.findById(request.cuestionarioId())
            .orElseThrow(() -> new EntityNotFoundException("Cuestionario", request.cuestionarioId()));

        if (!cuestionario.getActivo()) {
            throw new BusinessException("El cuestionario no esta activo");
        }

        asignacionCuestionarioService.validarAsignacionActiva(candidato.getId(), cuestionario.getId());

        List<IntentoExamen> intentosExistentes = intentoExamenRepository
            .findByCandidatoIdAndCuestionarioId(candidato.getId(), cuestionario.getId());

        boolean tieneEnProgreso = intentosExistentes.stream()
            .anyMatch(i -> i.getEstado() == EstadoIntento.EN_PROGRESO);

        if (tieneEnProgreso) {
            throw new BusinessException("Ya tiene un intento en progreso para este cuestionario");
        }

        long intentosUsados = intentosExistentes.stream()
            .filter(i -> i.getEstado() == EstadoIntento.FINALIZADO || i.getEstado() == EstadoIntento.ABANDONADO)
            .count();

        if (intentosUsados >= cuestionario.getMaxIntentos()) {
            throw new BusinessException("Ha alcanzado el maximo de intentos permitidos (" + cuestionario.getMaxIntentos() + ") para este cuestionario");
        }

        IntentoExamen intento = IntentoExamen.builder()
            .candidato(candidato)
            .cuestionario(cuestionario)
            .fechaInicio(LocalDateTime.now())
            .build();

        intento = intentoExamenRepository.save(intento);

        // Snapshot: pre-crear RespuestaCandidato vacías para todas las preguntas actuales
        List<Pregunta> preguntasActuales = preguntaRepository.findByCuestionarioId(cuestionario.getId());
        BigDecimal puntajeMaximo = preguntasActuales.stream()
            .map(Pregunta::getPuntaje)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        IntentoExamen finalIntento = intento;
        List<RespuestaCandidato> respuestasVacias = preguntasActuales.stream()
            .map(p -> RespuestaCandidato.builder()
                .intentoExamen(finalIntento)
                .pregunta(p)
                .build())
            .toList();

        respuestaCandidatoRepository.saveAll(respuestasVacias);
        intento.setPuntajeMaximo(puntajeMaximo);
        intento = intentoExamenRepository.save(intento);

        log.info("[CREAR] Intento iniciado — id: {}, candidato: {}, cuestionarioId: {}, preguntas: {}, puntajeMaximo: {}",
            intento.getId(), username, request.cuestionarioId(), preguntasActuales.size(), puntajeMaximo);
        return toResponse(intento);
    }

    public RespuestaCandidatoResponse enviarRespuesta(Long intentoId, EnviarRespuestaRequest request, String username) {
        IntentoExamen intento = intentoExamenRepository.findById(intentoId)
            .orElseThrow(() -> new EntityNotFoundException("IntentoExamen", intentoId));

        validarIntentoActivo(intento, username);

        RespuestaCandidato respuesta = respuestaCandidatoRepository
            .findByIntentoExamenIdAndPreguntaId(intentoId, request.preguntaId())
            .orElseThrow(() -> new BusinessException("La pregunta no pertenece a este intento de examen"));

        Pregunta pregunta = respuesta.getPregunta();

        respuesta.setCodigoFuente(request.codigoFuente());
        respuesta.setLenguaje(request.lenguaje());
        if (request.opcionesSeleccionadas() != null) {
            respuesta.setOpcionesSeleccionadas(request.opcionesSeleccionadas().toString());
        }

        calificacionService.calificar(respuesta, pregunta);

        respuesta = respuestaCandidatoRepository.save(respuesta);
        log.info("[CREAR] Respuesta enviada — intentoId: {}, preguntaId: {}, esCorrecta: {}", intentoId, request.preguntaId(), respuesta.getEsCorrecta());
        return toRespuestaResponse(respuesta);
    }

    public ResultadoIntentoResponse finalizarIntento(Long intentoId, String username) {
        IntentoExamen intento = intentoExamenRepository.findById(intentoId)
            .orElseThrow(() -> new EntityNotFoundException("IntentoExamen", intentoId));

        validarIntentoActivo(intento, username);

        intento.setFechaFin(LocalDateTime.now());
        intento.setEstado(EstadoIntento.FINALIZADO);
        intento.setTiempoConsumido(
            (int) ChronoUnit.SECONDS.between(intento.getFechaInicio(), intento.getFechaFin())
        );

        List<RespuestaCandidato> respuestas = respuestaCandidatoRepository.findByIntentoExamenId(intentoId);
        BigDecimal puntajeTotal = respuestas.stream()
            .map(RespuestaCandidato::getPuntajeObtenido)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        intento.setPuntajeTotal(puntajeTotal);
        intento = intentoExamenRepository.save(intento);
        log.info("[ACTUALIZAR] Intento finalizado — id: {}, candidato: {}, puntaje: {}", intentoId, username, puntajeTotal);

        return toResultadoResponse(intento, respuestas);
    }

    public void abandonarIntento(Long intentoId, String username) {
        IntentoExamen intento = intentoExamenRepository.findById(intentoId)
            .orElseThrow(() -> new EntityNotFoundException("IntentoExamen", intentoId));

        if (!intento.getCandidato().getUsername().equals(username)) {
            throw new BusinessException("Este intento no pertenece al usuario actual");
        }
        if (intento.getEstado() != EstadoIntento.EN_PROGRESO) {
            throw new BusinessException("Solo se pueden abandonar intentos en progreso");
        }

        intento.setFechaFin(LocalDateTime.now());
        intento.setEstado(EstadoIntento.ABANDONADO);
        intento.setTiempoConsumido(
            (int) ChronoUnit.SECONDS.between(intento.getFechaInicio(), intento.getFechaFin())
        );
        intento.setPuntajeTotal(BigDecimal.ZERO);
        intentoExamenRepository.save(intento);
        log.info("[ABANDONAR] Intento abandonado — id: {}, candidato: {}", intentoId, username);
    }

    @Transactional(readOnly = true)
    public IntentoExamenResponse buscarPorId(Long id) {
        IntentoExamen intento = intentoExamenRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("IntentoExamen", id));
        return toResponse(intento);
    }

    @Transactional(readOnly = true)
    public List<IntentoExamenResponse> listarPorCandidato(String username) {
        Usuario candidato = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("Usuario", username));
        return intentoExamenRepository.findByCandidatoIdOrderByCreatedAtDesc(candidato.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ResultadoIntentoResponse> listarResultadosPorCuestionario(Long cuestionarioId) {
        if (!cuestionarioRepository.existsById(cuestionarioId)) {
            throw new EntityNotFoundException("Cuestionario", cuestionarioId);
        }
        return intentoExamenRepository.findByCuestionarioId(cuestionarioId).stream()
            .filter(i -> i.getEstado() == EstadoIntento.FINALIZADO)
            .map(i -> {
                List<RespuestaCandidato> respuestas = respuestaCandidatoRepository.findByIntentoExamenId(i.getId());
                return toResultadoResponse(i, respuestas);
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public ResultadoIntentoResponse obtenerResultadoCandidato(Long intentoId, String username) {
        IntentoExamen intento = intentoExamenRepository.findById(intentoId)
            .orElseThrow(() -> new EntityNotFoundException("IntentoExamen", intentoId));

        if (!intento.getCandidato().getUsername().equals(username)) {
            throw new BusinessException("Este intento no pertenece al usuario actual");
        }

        List<RespuestaCandidato> respuestas = respuestaCandidatoRepository.findByIntentoExamenId(intentoId);
        return toResultadoResponse(intento, respuestas);
    }

    @Transactional(readOnly = true)
    public IntentoExamenResponse buscarPorIdConValidacion(Long id, String username, boolean isAdmin) {
        IntentoExamen intento = intentoExamenRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("IntentoExamen", id));

        if (!isAdmin && !intento.getCandidato().getUsername().equals(username)) {
            throw new BusinessException("Este intento no pertenece al usuario actual");
        }

        return toResponse(intento);
    }

    @Transactional(readOnly = true)
    public ResultadoIntentoResponse obtenerResultadoDetallado(Long intentoId) {
        IntentoExamen intento = intentoExamenRepository.findById(intentoId)
            .orElseThrow(() -> new EntityNotFoundException("IntentoExamen", intentoId));
        List<RespuestaCandidato> respuestas = respuestaCandidatoRepository.findByIntentoExamenId(intentoId);
        return toResultadoResponse(intento, respuestas);
    }

    @Transactional(readOnly = true)
    public List<PreguntaResponse> obtenerPreguntasDelIntento(Long intentoId, String username, boolean isAdmin) {
        IntentoExamen intento = intentoExamenRepository.findById(intentoId)
            .orElseThrow(() -> new EntityNotFoundException("IntentoExamen", intentoId));

        if (!isAdmin && !intento.getCandidato().getUsername().equals(username)) {
            throw new BusinessException("Este intento no pertenece al usuario actual");
        }

        List<RespuestaCandidato> respuestas = respuestaCandidatoRepository.findByIntentoExamenId(intentoId);
        return respuestas.stream()
            .map(r -> preguntaService.toResponse(r.getPregunta()))
            .toList();
    }

    private void validarIntentoActivo(IntentoExamen intento, String username) {
        if (!intento.getCandidato().getUsername().equals(username)) {
            throw new BusinessException("Este intento no pertenece al usuario actual");
        }
        if (intento.getEstado() != EstadoIntento.EN_PROGRESO) {
            throw new BusinessException("Este intento ya fue finalizado");
        }
    }

    private IntentoExamenResponse toResponse(IntentoExamen i) {
        return new IntentoExamenResponse(
            i.getId(),
            i.getCandidato().getId(),
            i.getCandidato().getNombreCompleto(),
            i.getCuestionario().getId(),
            i.getCuestionario().getNombre(),
            i.getFechaInicio(),
            i.getFechaFin(),
            i.getEstado(),
            i.getPuntajeTotal(),
            i.getPuntajeMaximo(),
            i.getTiempoConsumido()
        );
    }

    private ResultadoIntentoResponse toResultadoResponse(IntentoExamen i, List<RespuestaCandidato> respuestas) {
        return new ResultadoIntentoResponse(
            i.getId(),
            i.getCandidato().getId(),
            i.getCandidato().getNombreCompleto(),
            i.getCuestionario().getId(),
            i.getCuestionario().getNombre(),
            i.getFechaInicio(),
            i.getFechaFin(),
            i.getEstado(),
            i.getPuntajeTotal(),
            i.getPuntajeMaximo(),
            i.getTiempoConsumido(),
            respuestas.stream().map(this::toRespuestaResponse).toList()
        );
    }

    private RespuestaCandidatoResponse toRespuestaResponse(RespuestaCandidato r) {
        return new RespuestaCandidatoResponse(
            r.getId(),
            r.getPregunta().getId(),
            r.getPregunta().getTitulo(),
            r.getCodigoFuente(),
            r.getLenguaje(),
            r.getOpcionesSeleccionadas(),
            r.getResultadoEjecucion(),
            r.getSalidaObtenida(),
            r.getEsCorrecta(),
            r.getPuntajeObtenido()
        );
    }
}
