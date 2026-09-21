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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final OpcionRespuestaRepository opcionRespuestaRepository;
    private final CasoDePruebaRepository casoDePruebaRepository;

    public PreguntaResponse crear(Long cuestionarioId, CreatePreguntaRequest request) {
        Cuestionario cuestionario = cuestionarioRepository.findById(cuestionarioId)
            .orElseThrow(() -> new EntityNotFoundException("Cuestionario", cuestionarioId));

        validarTipoYLenguajes(request.tipoPregunta(), request.lenguajesPermitidos());

        Pregunta pregunta = Pregunta.builder()
            .titulo(request.titulo())
            .descripcion(request.descripcion())
            .tipoPregunta(request.tipoPregunta())
            .lenguajesPermitidos(request.lenguajesPermitidos() != null ? new ArrayList<>(request.lenguajesPermitidos()) : new ArrayList<>())
            .puntaje(request.puntaje() != null ? request.puntaje() : BigDecimal.ONE)
            .build();

        pregunta = preguntaRepository.save(pregunta);
        cuestionario.getPreguntas().add(pregunta);
        cuestionarioRepository.save(cuestionario);
        actualizarCantidadPreguntas(cuestionarioId);
        log.info("[CREAR] Pregunta creada — id: {}, tipo: {}, cuestionarioId: {}", pregunta.getId(), pregunta.getTipoPregunta(), cuestionarioId);
        return toResponse(pregunta);
    }

    @Transactional(readOnly = true)
    public List<PreguntaResponse> listarTodas() {
        return preguntaRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    public PreguntaResponse duplicar(Long preguntaId, Long cuestionarioId) {
        Pregunta original = preguntaRepository.findById(preguntaId)
            .orElseThrow(() -> new EntityNotFoundException("Pregunta", preguntaId));
        Cuestionario destino = cuestionarioRepository.findById(cuestionarioId)
            .orElseThrow(() -> new EntityNotFoundException("Cuestionario", cuestionarioId));

        Pregunta copia = Pregunta.builder()
            .titulo(original.getTitulo())
            .descripcion(original.getDescripcion())
            .tipoPregunta(original.getTipoPregunta())
            .lenguajesPermitidos(new ArrayList<>(original.getLenguajesPermitidos()))
            .puntaje(original.getPuntaje())
            .build();

        copia = preguntaRepository.save(copia);
        destino.getPreguntas().add(copia);
        cuestionarioRepository.save(destino);

        for (OpcionRespuesta op : original.getOpciones()) {
            OpcionRespuesta copiaOp = OpcionRespuesta.builder()
                .texto(op.getTexto())
                .esCorrecta(op.getEsCorrecta())
                .pregunta(copia)
                .build();
            opcionRespuestaRepository.save(copiaOp);
        }

        for (CasoDePrueba caso : original.getCasosDePrueba()) {
            CasoDePrueba copiaCaso = CasoDePrueba.builder()
                .input(caso.getInput())
                .expectedOutput(caso.getExpectedOutput())
                .pregunta(copia)
                .build();
            casoDePruebaRepository.save(copiaCaso);
        }

        actualizarCantidadPreguntas(cuestionarioId);
        log.info("[DUPLICAR] Pregunta duplicada — originalId: {}, copiaId: {}, cuestionarioDestinoId: {}", preguntaId, copia.getId(), cuestionarioId);
        return buscarPorId(copia.getId());
    }

    public PreguntaResponse asociar(Long preguntaId, Long cuestionarioId) {
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
            .orElseThrow(() -> new EntityNotFoundException("Pregunta", preguntaId));
        Cuestionario cuestionario = cuestionarioRepository.findById(cuestionarioId)
            .orElseThrow(() -> new EntityNotFoundException("Cuestionario", cuestionarioId));

        if (cuestionario.getPreguntas().contains(pregunta)) {
            throw new BusinessException("La pregunta ya esta asociada a este cuestionario");
        }
        cuestionario.getPreguntas().add(pregunta);
        cuestionarioRepository.save(cuestionario);
        actualizarCantidadPreguntas(cuestionarioId);
        log.info("[ASOCIAR] Pregunta asociada — preguntaId: {}, cuestionarioId: {}", preguntaId, cuestionarioId);
        return toResponse(pregunta);
    }

    public void desvincular(Long preguntaId, Long cuestionarioId) {
        Cuestionario cuestionario = cuestionarioRepository.findById(cuestionarioId)
            .orElseThrow(() -> new EntityNotFoundException("Cuestionario", cuestionarioId));
        cuestionario.getPreguntas().removeIf(p -> p.getId().equals(preguntaId));
        cuestionarioRepository.save(cuestionario);
        actualizarCantidadPreguntas(cuestionarioId);
        log.info("[DESVINCULAR] Pregunta desvinculada — preguntaId: {}, cuestionarioId: {}", preguntaId, cuestionarioId);
    }

    @Transactional(readOnly = true)
    public List<PreguntaResponse> listarPorCuestionario(Long cuestionarioId) {
        if (!cuestionarioRepository.existsById(cuestionarioId)) {
            throw new EntityNotFoundException("Cuestionario", cuestionarioId);
        }
        return preguntaRepository.findByCuestionarioId(cuestionarioId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PreguntaResponse buscarPorId(Long id) {
        Pregunta pregunta = preguntaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pregunta", id));
        return toResponse(pregunta);
    }

    public PreguntaResponse actualizar(Long id, UpdatePreguntaRequest request) {
        Pregunta pregunta = preguntaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pregunta", id));

        if (request.tipoPregunta() != null || request.lenguajesPermitidos() != null) {
            TipoPregunta tipo = request.tipoPregunta() != null ? request.tipoPregunta() : pregunta.getTipoPregunta();
            List<LenguajeProgramacion> lenguajes = request.lenguajesPermitidos() != null ? request.lenguajesPermitidos() : pregunta.getLenguajesPermitidos();
            validarTipoYLenguajes(tipo, lenguajes);
        }

        if (request.titulo() != null) pregunta.setTitulo(request.titulo());
        if (request.descripcion() != null) pregunta.setDescripcion(request.descripcion());
        if (request.tipoPregunta() != null) pregunta.setTipoPregunta(request.tipoPregunta());
        if (request.lenguajesPermitidos() != null) pregunta.setLenguajesPermitidos(new ArrayList<>(request.lenguajesPermitidos()));
        if (request.puntaje() != null) pregunta.setPuntaje(request.puntaje());

        pregunta = preguntaRepository.save(pregunta);
        log.info("[ACTUALIZAR] Pregunta actualizada — id: {}", id);
        return toResponse(pregunta);
    }

    public void eliminar(Long id) {
        Pregunta pregunta = preguntaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pregunta", id));

        // Desvincular de todos los cuestionarios antes de eliminar
        for (Cuestionario c : new ArrayList<>(pregunta.getCuestionarios())) {
            c.getPreguntas().remove(pregunta);
            cuestionarioRepository.save(c);
            actualizarCantidadPreguntas(c.getId());
        }

        preguntaRepository.delete(pregunta);
        log.info("[ELIMINAR] Pregunta eliminada — id: {}", id);
    }

    public OpcionRespuestaResponse agregarOpcion(Long preguntaId, CreateOpcionRequest request) {
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
            .orElseThrow(() -> new EntityNotFoundException("Pregunta", preguntaId));

        if (pregunta.getTipoPregunta() == TipoPregunta.CODIGO) {
            throw new BusinessException("No se pueden agregar opciones a preguntas de tipo CODIGO");
        }

        OpcionRespuesta opcion = OpcionRespuesta.builder()
            .texto(request.texto())
            .esCorrecta(request.esCorrecta() != null ? request.esCorrecta() : false)
            .pregunta(pregunta)
            .build();

        opcion = opcionRespuestaRepository.save(opcion);
        log.info("[CREAR] Opcion agregada — id: {}, preguntaId: {}", opcion.getId(), preguntaId);
        return toOpcionResponse(opcion);
    }

    public void eliminarOpcion(Long opcionId) {
        if (!opcionRespuestaRepository.existsById(opcionId)) {
            throw new EntityNotFoundException("OpcionRespuesta", opcionId);
        }
        opcionRespuestaRepository.deleteById(opcionId);
        log.info("[ELIMINAR] Opcion eliminada — id: {}", opcionId);
    }

    public CasoDePruebaResponse agregarCasoDePrueba(Long preguntaId, CreateCasoDePruebaRequest request) {
        Pregunta pregunta = preguntaRepository.findById(preguntaId)
            .orElseThrow(() -> new EntityNotFoundException("Pregunta", preguntaId));

        if (pregunta.getTipoPregunta() != TipoPregunta.CODIGO) {
            throw new BusinessException("Solo se pueden agregar casos de prueba a preguntas de tipo CODIGO");
        }

        CasoDePrueba caso = CasoDePrueba.builder()
            .input(request.input())
            .expectedOutput(request.expectedOutput())
            .pregunta(pregunta)
            .build();

        caso = casoDePruebaRepository.save(caso);
        log.info("[CREAR] Caso de prueba agregado — id: {}, preguntaId: {}", caso.getId(), preguntaId);
        return toCasoResponse(caso);
    }

    public void eliminarCasoDePrueba(Long casoId) {
        if (!casoDePruebaRepository.existsById(casoId)) {
            throw new EntityNotFoundException("CasoDePrueba", casoId);
        }
        casoDePruebaRepository.deleteById(casoId);
        log.info("[ELIMINAR] Caso de prueba eliminado — id: {}", casoId);
    }

    private void validarTipoYLenguajes(TipoPregunta tipo, List<LenguajeProgramacion> lenguajes) {
        if (tipo == TipoPregunta.CODIGO && (lenguajes == null || lenguajes.isEmpty())) {
            throw new BusinessException("Las preguntas de tipo CODIGO requieren al menos un lenguaje permitido");
        }
        if (tipo != TipoPregunta.CODIGO && lenguajes != null && !lenguajes.isEmpty()) {
            throw new BusinessException("Solo las preguntas de tipo CODIGO pueden tener lenguajes permitidos");
        }
    }

    private void actualizarCantidadPreguntas(Long cuestionarioId) {
        Cuestionario cuestionario = cuestionarioRepository.findById(cuestionarioId).orElseThrow();
        cuestionario.setCantidadPreguntas(preguntaRepository.countByCuestionarioId(cuestionarioId));
        cuestionarioRepository.save(cuestionario);
    }

    public PreguntaResponse toResponse(Pregunta p) {
        List<CuestionarioSimpleResponse> cuestionarios = p.getCuestionarios().stream()
            .map(c -> new CuestionarioSimpleResponse(c.getId(), c.getNombre()))
            .toList();
        return new PreguntaResponse(
            p.getId(),
            p.getTitulo(),
            p.getDescripcion(),
            p.getTipoPregunta(),
            p.getLenguajesPermitidos(),
            p.getPuntaje(),
            cuestionarios,
            p.getOpciones().stream().map(this::toOpcionResponse).toList(),
            p.getCasosDePrueba().stream().map(this::toCasoResponse).toList()
        );
    }

    public PreguntaResponse toResponseForCandidato(Pregunta p) {
        return new PreguntaResponse(
            p.getId(),
            p.getTitulo(),
            p.getDescripcion(),
            p.getTipoPregunta(),
            p.getLenguajesPermitidos(),
            p.getPuntaje(),
            List.of(),
            p.getOpciones().stream()
                .map(o -> new OpcionRespuestaResponse(o.getId(), o.getTexto(), null))
                .toList(),
            List.of()
        );
    }

    private OpcionRespuestaResponse toOpcionResponse(OpcionRespuesta o) {
        return new OpcionRespuestaResponse(o.getId(), o.getTexto(), o.getEsCorrecta());
    }

    private CasoDePruebaResponse toCasoResponse(CasoDePrueba c) {
        return new CasoDePruebaResponse(c.getId(), c.getInput(), c.getExpectedOutput());
    }
}
