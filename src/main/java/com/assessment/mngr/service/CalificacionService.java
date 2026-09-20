package com.assessment.mngr.service;

import com.assessment.mngr.client.CompilerClient;
import com.assessment.mngr.controller.dto.CompilerRequest;
import com.assessment.mngr.controller.dto.CompilerResponse;
import com.assessment.mngr.model.*;
import com.assessment.mngr.repository.OpcionRespuestaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalificacionService {

    private final CompilerClient compilerClient;
    private final OpcionRespuestaRepository opcionRespuestaRepository;

    public void calificar(RespuestaCandidato respuesta, Pregunta pregunta) {
        switch (pregunta.getTipoPregunta()) {
            case OPCION_UNICA -> calificarOpcionUnica(respuesta, pregunta);
            case OPCION_MULTIPLE -> calificarOpcionMultiple(respuesta, pregunta);
            case CODIGO -> calificarCodigo(respuesta, pregunta);
        }
    }

    private void calificarOpcionUnica(RespuestaCandidato respuesta, Pregunta pregunta) {
        if (respuesta.getOpcionesSeleccionadas() == null || respuesta.getOpcionesSeleccionadas().isBlank()) {
            respuesta.setEsCorrecta(false);
            respuesta.setPuntajeObtenido(BigDecimal.ZERO);
            return;
        }

        List<Long> seleccionadas = parseOpciones(respuesta.getOpcionesSeleccionadas());
        if (seleccionadas.size() != 1) {
            respuesta.setEsCorrecta(false);
            respuesta.setPuntajeObtenido(BigDecimal.ZERO);
            return;
        }

        Long seleccionada = seleccionadas.get(0);
        List<OpcionRespuesta> opciones = opcionRespuestaRepository.findByPreguntaId(pregunta.getId());
        boolean correcta = opciones.stream()
            .filter(OpcionRespuesta::getEsCorrecta)
            .anyMatch(o -> o.getId().equals(seleccionada));

        respuesta.setEsCorrecta(correcta);
        respuesta.setPuntajeObtenido(correcta ? pregunta.getPuntaje() : BigDecimal.ZERO);
    }

    private void calificarOpcionMultiple(RespuestaCandidato respuesta, Pregunta pregunta) {
        if (respuesta.getOpcionesSeleccionadas() == null || respuesta.getOpcionesSeleccionadas().isBlank()) {
            respuesta.setEsCorrecta(false);
            respuesta.setPuntajeObtenido(BigDecimal.ZERO);
            return;
        }

        List<Long> seleccionadas = parseOpciones(respuesta.getOpcionesSeleccionadas());
        List<OpcionRespuesta> opciones = opcionRespuestaRepository.findByPreguntaId(pregunta.getId());

        Set<Long> correctasIds = opciones.stream()
            .filter(OpcionRespuesta::getEsCorrecta)
            .map(OpcionRespuesta::getId)
            .collect(Collectors.toSet());

        Set<Long> seleccionadasSet = Set.copyOf(seleccionadas);
        boolean correcta = correctasIds.equals(seleccionadasSet);

        respuesta.setEsCorrecta(correcta);
        respuesta.setPuntajeObtenido(correcta ? pregunta.getPuntaje() : BigDecimal.ZERO);
    }

    private void calificarCodigo(RespuestaCandidato respuesta, Pregunta pregunta) {
        if (respuesta.getCodigoFuente() == null || respuesta.getCodigoFuente().isBlank()) {
            respuesta.setResultadoEjecucion(ResultadoEjecucion.NO_EJECUTADO);
            respuesta.setEsCorrecta(false);
            respuesta.setPuntajeObtenido(BigDecimal.ZERO);
            return;
        }

        List<CompilerRequest.TestCaseDto> testCases = pregunta.getCasosDePrueba().stream()
            .map(c -> new CompilerRequest.TestCaseDto(c.getInput(), c.getExpectedOutput()))
            .toList();

        CompilerRequest compilerRequest = new CompilerRequest(
            respuesta.getCodigoFuente(),
            respuesta.getLenguaje() != null ? respuesta.getLenguaje() : pregunta.getLenguajesPermitidos().get(0).name().toLowerCase(),
            testCases
        );

        CompilerResponse compilerResponse = compilerClient.execute(compilerRequest);

        if (compilerResponse == null) {
            respuesta.setResultadoEjecucion(ResultadoEjecucion.ERROR_EJECUCION);
            respuesta.setSalidaObtenida("Sin respuesta del compilador");
            respuesta.setEsCorrecta(false);
            respuesta.setPuntajeObtenido(BigDecimal.ZERO);
            return;
        }

        respuesta.setSalidaObtenida(compilerResponse.output());

        if (!compilerResponse.success()) {
            respuesta.setResultadoEjecucion(
                compilerResponse.error() != null && compilerResponse.error().contains("compilation")
                    ? ResultadoEjecucion.ERROR_COMPILACION
                    : ResultadoEjecucion.ERROR_EJECUCION
            );
            respuesta.setEsCorrecta(false);
            respuesta.setPuntajeObtenido(BigDecimal.ZERO);
            return;
        }

        boolean allPassed = compilerResponse.testResults() != null &&
            compilerResponse.testResults().stream().allMatch(CompilerResponse.TestResult::passed);

        respuesta.setResultadoEjecucion(ResultadoEjecucion.EXITOSO);
        respuesta.setEsCorrecta(allPassed);
        respuesta.setPuntajeObtenido(allPassed ? pregunta.getPuntaje() : BigDecimal.ZERO);
    }

    private List<Long> parseOpciones(String opcionesJson) {
        String cleaned = opcionesJson.replaceAll("[\\[\\]\\s]", "");
        if (cleaned.isEmpty()) return List.of();
        return List.of(cleaned.split(",")).stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Long::parseLong)
            .toList();
    }
}
