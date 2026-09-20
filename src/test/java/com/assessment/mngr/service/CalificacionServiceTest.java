package com.assessment.mngr.service;

import com.assessment.mngr.client.CompilerClient;
import com.assessment.mngr.controller.dto.CompilerRequest;
import com.assessment.mngr.controller.dto.CompilerResponse;
import com.assessment.mngr.model.*;
import com.assessment.mngr.repository.OpcionRespuestaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalificacionServiceTest {

    @Mock
    private CompilerClient compilerClient;

    @Mock
    private OpcionRespuestaRepository opcionRespuestaRepository;

    @InjectMocks
    private CalificacionService calificacionService;

    private Pregunta preguntaUnica;
    private Pregunta preguntaMultiple;
    private Pregunta preguntaCodigo;

    @BeforeEach
    void setUp() {
        preguntaUnica = Pregunta.builder()
            .id(1L)
            .titulo("Pregunta unica")
            .tipoPregunta(TipoPregunta.OPCION_UNICA)
            .puntaje(new BigDecimal("10.00"))
            .build();

        preguntaMultiple = Pregunta.builder()
            .id(2L)
            .titulo("Pregunta multiple")
            .tipoPregunta(TipoPregunta.OPCION_MULTIPLE)
            .puntaje(new BigDecimal("15.00"))
            .build();

        preguntaCodigo = Pregunta.builder()
            .id(3L)
            .titulo("Pregunta codigo")
            .tipoPregunta(TipoPregunta.CODIGO)
            .puntaje(new BigDecimal("20.00"))
            .lenguajesPermitidos(List.of(LenguajeProgramacion.JAVA))
            .casosDePrueba(List.of(
                CasoDePrueba.builder().id(1L).input("5").expectedOutput("25").build(),
                CasoDePrueba.builder().id(2L).input("3").expectedOutput("9").build()
            ))
            .build();
    }

    @Nested
    @DisplayName("Calificar Opcion Unica")
    class OpcionUnicaTests {

        @Test
        @DisplayName("respuesta correcta otorga puntaje completo")
        void respuestaCorrecta() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .opcionesSeleccionadas("[10]")
                .build();

            OpcionRespuesta correcta = OpcionRespuesta.builder().id(10L).esCorrecta(true).build();
            OpcionRespuesta incorrecta = OpcionRespuesta.builder().id(11L).esCorrecta(false).build();
            when(opcionRespuestaRepository.findByPreguntaId(1L)).thenReturn(List.of(correcta, incorrecta));

            calificacionService.calificar(respuesta, preguntaUnica);

            assertThat(respuesta.getEsCorrecta()).isTrue();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(new BigDecimal("10.00"));
        }

        @Test
        @DisplayName("respuesta incorrecta otorga cero puntos")
        void respuestaIncorrecta() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .opcionesSeleccionadas("[11]")
                .build();

            OpcionRespuesta correcta = OpcionRespuesta.builder().id(10L).esCorrecta(true).build();
            OpcionRespuesta incorrecta = OpcionRespuesta.builder().id(11L).esCorrecta(false).build();
            when(opcionRespuestaRepository.findByPreguntaId(1L)).thenReturn(List.of(correcta, incorrecta));

            calificacionService.calificar(respuesta, preguntaUnica);

            assertThat(respuesta.getEsCorrecta()).isFalse();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("opciones null otorga cero puntos")
        void opcionesNull() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .opcionesSeleccionadas(null)
                .build();

            calificacionService.calificar(respuesta, preguntaUnica);

            assertThat(respuesta.getEsCorrecta()).isFalse();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("opciones vacias otorga cero puntos")
        void opcionesVacias() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .opcionesSeleccionadas("  ")
                .build();

            calificacionService.calificar(respuesta, preguntaUnica);

            assertThat(respuesta.getEsCorrecta()).isFalse();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("mas de una opcion seleccionada otorga cero puntos")
        void multiples_opciones_seleccionadas() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .opcionesSeleccionadas("[10, 11]")
                .build();

            calificacionService.calificar(respuesta, preguntaUnica);

            assertThat(respuesta.getEsCorrecta()).isFalse();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Calificar Opcion Multiple")
    class OpcionMultipleTests {

        @Test
        @DisplayName("todas las opciones correctas seleccionadas otorga puntaje completo")
        void todasCorrectas() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .opcionesSeleccionadas("[20, 21]")
                .build();

            OpcionRespuesta c1 = OpcionRespuesta.builder().id(20L).esCorrecta(true).build();
            OpcionRespuesta c2 = OpcionRespuesta.builder().id(21L).esCorrecta(true).build();
            OpcionRespuesta inc = OpcionRespuesta.builder().id(22L).esCorrecta(false).build();
            when(opcionRespuestaRepository.findByPreguntaId(2L)).thenReturn(List.of(c1, c2, inc));

            calificacionService.calificar(respuesta, preguntaMultiple);

            assertThat(respuesta.getEsCorrecta()).isTrue();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(new BigDecimal("15.00"));
        }

        @Test
        @DisplayName("seleccion parcial otorga cero puntos")
        void seleccionParcial() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .opcionesSeleccionadas("[20]")
                .build();

            OpcionRespuesta c1 = OpcionRespuesta.builder().id(20L).esCorrecta(true).build();
            OpcionRespuesta c2 = OpcionRespuesta.builder().id(21L).esCorrecta(true).build();
            when(opcionRespuestaRepository.findByPreguntaId(2L)).thenReturn(List.of(c1, c2));

            calificacionService.calificar(respuesta, preguntaMultiple);

            assertThat(respuesta.getEsCorrecta()).isFalse();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("seleccion con opcion incorrecta extra otorga cero puntos")
        void seleccionConExtra() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .opcionesSeleccionadas("[20, 21, 22]")
                .build();

            OpcionRespuesta c1 = OpcionRespuesta.builder().id(20L).esCorrecta(true).build();
            OpcionRespuesta c2 = OpcionRespuesta.builder().id(21L).esCorrecta(true).build();
            OpcionRespuesta inc = OpcionRespuesta.builder().id(22L).esCorrecta(false).build();
            when(opcionRespuestaRepository.findByPreguntaId(2L)).thenReturn(List.of(c1, c2, inc));

            calificacionService.calificar(respuesta, preguntaMultiple);

            assertThat(respuesta.getEsCorrecta()).isFalse();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("opciones null en multiple otorga cero puntos")
        void opcionesNull() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .opcionesSeleccionadas(null)
                .build();

            calificacionService.calificar(respuesta, preguntaMultiple);

            assertThat(respuesta.getEsCorrecta()).isFalse();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Calificar Codigo")
    class CodigoTests {

        @Test
        @DisplayName("codigo null marca NO_EJECUTADO y cero puntos")
        void codigoNull() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .codigoFuente(null)
                .build();

            calificacionService.calificar(respuesta, preguntaCodigo);

            assertThat(respuesta.getResultadoEjecucion()).isEqualTo(ResultadoEjecucion.NO_EJECUTADO);
            assertThat(respuesta.getEsCorrecta()).isFalse();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(BigDecimal.ZERO);
            verifyNoInteractions(compilerClient);
        }

        @Test
        @DisplayName("codigo vacio marca NO_EJECUTADO")
        void codigoVacio() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .codigoFuente("   ")
                .build();

            calificacionService.calificar(respuesta, preguntaCodigo);

            assertThat(respuesta.getResultadoEjecucion()).isEqualTo(ResultadoEjecucion.NO_EJECUTADO);
            verifyNoInteractions(compilerClient);
        }

        @Test
        @DisplayName("todos los test cases pasan otorga puntaje completo")
        void todosLosCasesPasan() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .codigoFuente("public class Main { ... }")
                .lenguaje("java")
                .build();

            CompilerResponse response = new CompilerResponse(true, "25\n9", null, List.of(
                new CompilerResponse.TestResult("5", "25", "25", true),
                new CompilerResponse.TestResult("3", "9", "9", true)
            ));
            when(compilerClient.execute(any(CompilerRequest.class))).thenReturn(response);

            calificacionService.calificar(respuesta, preguntaCodigo);

            assertThat(respuesta.getResultadoEjecucion()).isEqualTo(ResultadoEjecucion.EXITOSO);
            assertThat(respuesta.getEsCorrecta()).isTrue();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(new BigDecimal("20.00"));
        }

        @Test
        @DisplayName("algun test case falla otorga cero puntos")
        void algunCaseFalla() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .codigoFuente("public class Main { ... }")
                .lenguaje("java")
                .build();

            CompilerResponse response = new CompilerResponse(true, "25\n10", null, List.of(
                new CompilerResponse.TestResult("5", "25", "25", true),
                new CompilerResponse.TestResult("3", "9", "10", false)
            ));
            when(compilerClient.execute(any(CompilerRequest.class))).thenReturn(response);

            calificacionService.calificar(respuesta, preguntaCodigo);

            assertThat(respuesta.getResultadoEjecucion()).isEqualTo(ResultadoEjecucion.EXITOSO);
            assertThat(respuesta.getEsCorrecta()).isFalse();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("error de compilacion marca ERROR_COMPILACION")
        void errorCompilacion() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .codigoFuente("invalid code {{{")
                .lenguaje("java")
                .build();

            CompilerResponse response = new CompilerResponse(false, null, "compilation error: expected ';'", null);
            when(compilerClient.execute(any(CompilerRequest.class))).thenReturn(response);

            calificacionService.calificar(respuesta, preguntaCodigo);

            assertThat(respuesta.getResultadoEjecucion()).isEqualTo(ResultadoEjecucion.ERROR_COMPILACION);
            assertThat(respuesta.getEsCorrecta()).isFalse();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("error de ejecucion sin keyword compilation marca ERROR_EJECUCION")
        void errorEjecucion() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .codigoFuente("public class Main { public static void main(String[] args) { throw new RuntimeException(); } }")
                .lenguaje("java")
                .build();

            CompilerResponse response = new CompilerResponse(false, null, "runtime exception", null);
            when(compilerClient.execute(any(CompilerRequest.class))).thenReturn(response);

            calificacionService.calificar(respuesta, preguntaCodigo);

            assertThat(respuesta.getResultadoEjecucion()).isEqualTo(ResultadoEjecucion.ERROR_EJECUCION);
            assertThat(respuesta.getEsCorrecta()).isFalse();
        }

        @Test
        @DisplayName("respuesta null del compilador marca ERROR_EJECUCION")
        void compilerRetornaNull() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .codigoFuente("public class Main { ... }")
                .lenguaje("java")
                .build();

            when(compilerClient.execute(any(CompilerRequest.class))).thenReturn(null);

            calificacionService.calificar(respuesta, preguntaCodigo);

            assertThat(respuesta.getResultadoEjecucion()).isEqualTo(ResultadoEjecucion.ERROR_EJECUCION);
            assertThat(respuesta.getSalidaObtenida()).isEqualTo("Sin respuesta del compilador");
            assertThat(respuesta.getEsCorrecta()).isFalse();
            assertThat(respuesta.getPuntajeObtenido()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("usa lenguaje de la pregunta si la respuesta no tiene lenguaje")
        void usaLenguajeDePreguntaSiNoHayEnRespuesta() {
            RespuestaCandidato respuesta = RespuestaCandidato.builder()
                .codigoFuente("code")
                .lenguaje(null)
                .build();

            CompilerResponse response = new CompilerResponse(true, "ok", null, List.of(
                new CompilerResponse.TestResult("5", "25", "25", true),
                new CompilerResponse.TestResult("3", "9", "9", true)
            ));
            when(compilerClient.execute(any(CompilerRequest.class))).thenReturn(response);

            calificacionService.calificar(respuesta, preguntaCodigo);

            verify(compilerClient).execute(argThat(req ->
                req.language().equals("java")
            ));
        }
    }
}
