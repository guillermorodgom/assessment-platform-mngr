package com.assessment.mngr.service;

import com.assessment.mngr.controller.dto.*;
import com.assessment.mngr.exception.BusinessException;
import com.assessment.mngr.exception.EntityNotFoundException;
import com.assessment.mngr.model.*;
import com.assessment.mngr.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreguntaServiceTest {

    @Mock
    private PreguntaRepository preguntaRepository;
    @Mock
    private CuestionarioRepository cuestionarioRepository;
    @Mock
    private OpcionRespuestaRepository opcionRespuestaRepository;
    @Mock
    private CasoDePruebaRepository casoDePruebaRepository;

    @InjectMocks
    private PreguntaService preguntaService;

    private Cuestionario cuestionario;
    private Pregunta preguntaUnica;
    private Pregunta preguntaCodigo;

    @BeforeEach
    void setUp() {
        Usuario admin = Usuario.builder()
            .id(1L).username("admin").nombreCompleto("Admin").email("a@t.com").password("h").build();

        cuestionario = Cuestionario.builder()
            .id(1L).nombre("Quiz").tiempoLimite(60).cantidadPreguntas(0)
            .maxIntentos(3).activo(true).creadoPor(admin).preguntas(new ArrayList<>()).build();

        preguntaUnica = Pregunta.builder()
            .id(10L).titulo("P Unica").tipoPregunta(TipoPregunta.OPCION_UNICA)
            .puntaje(new BigDecimal("5.00"))
            .cuestionarios(new ArrayList<>()).opciones(new ArrayList<>()).casosDePrueba(new ArrayList<>())
            .lenguajesPermitidos(new ArrayList<>())
            .build();

        preguntaCodigo = Pregunta.builder()
            .id(20L).titulo("P Codigo").tipoPregunta(TipoPregunta.CODIGO)
            .puntaje(new BigDecimal("10.00"))
            .lenguajesPermitidos(new ArrayList<>(List.of(LenguajeProgramacion.JAVA)))
            .cuestionarios(new ArrayList<>()).opciones(new ArrayList<>()).casosDePrueba(new ArrayList<>())
            .build();
    }

    @Nested
    @DisplayName("Crear pregunta")
    class CrearTests {

        @Test
        @DisplayName("crea pregunta OPCION_UNICA sin lenguajes")
        void crearOpcionUnica() {
            CreatePreguntaRequest request = new CreatePreguntaRequest(
                "Titulo", "Desc", TipoPregunta.OPCION_UNICA, null, new BigDecimal("5.00"));

            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(preguntaRepository.save(any(Pregunta.class))).thenReturn(preguntaUnica);
            when(cuestionarioRepository.save(any(Cuestionario.class))).thenReturn(cuestionario);
            when(preguntaRepository.countByCuestionarioId(1L)).thenReturn(1);

            PreguntaResponse response = preguntaService.crear(1L, request);

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.tipoPregunta()).isEqualTo(TipoPregunta.OPCION_UNICA);
            verify(preguntaRepository).save(any(Pregunta.class));
        }

        @Test
        @DisplayName("crea pregunta CODIGO con lenguajes")
        void crearCodigo() {
            CreatePreguntaRequest request = new CreatePreguntaRequest(
                "Codigo", "Desc", TipoPregunta.CODIGO, List.of(LenguajeProgramacion.JAVA), new BigDecimal("10.00"));

            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(preguntaRepository.save(any(Pregunta.class))).thenReturn(preguntaCodigo);
            when(cuestionarioRepository.save(any(Cuestionario.class))).thenReturn(cuestionario);
            when(preguntaRepository.countByCuestionarioId(1L)).thenReturn(1);

            PreguntaResponse response = preguntaService.crear(1L, request);

            assertThat(response.tipoPregunta()).isEqualTo(TipoPregunta.CODIGO);
        }

        @Test
        @DisplayName("falla si CODIGO sin lenguajes")
        void crearCodigoSinLenguajes() {
            CreatePreguntaRequest request = new CreatePreguntaRequest(
                "Codigo", "Desc", TipoPregunta.CODIGO, null, new BigDecimal("10.00"));

            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));

            assertThatThrownBy(() -> preguntaService.crear(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("requieren al menos un lenguaje");
        }

        @Test
        @DisplayName("falla si OPCION_UNICA con lenguajes")
        void crearUnicaConLenguajes() {
            CreatePreguntaRequest request = new CreatePreguntaRequest(
                "Unica", "Desc", TipoPregunta.OPCION_UNICA, List.of(LenguajeProgramacion.JAVA), new BigDecimal("5.00"));

            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));

            assertThatThrownBy(() -> preguntaService.crear(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Solo las preguntas de tipo CODIGO");
        }

        @Test
        @DisplayName("falla si cuestionario no existe")
        void cuestionarioNoExiste() {
            CreatePreguntaRequest request = new CreatePreguntaRequest(
                "T", "D", TipoPregunta.OPCION_UNICA, null, BigDecimal.ONE);
            when(cuestionarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> preguntaService.crear(99L, request))
                .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("usa puntaje por defecto cuando es null")
        void puntajePorDefecto() {
            CreatePreguntaRequest request = new CreatePreguntaRequest(
                "T", "D", TipoPregunta.OPCION_UNICA, null, null);

            Pregunta saved = Pregunta.builder()
                .id(30L).titulo("T").tipoPregunta(TipoPregunta.OPCION_UNICA)
                .puntaje(BigDecimal.ONE).cuestionarios(new ArrayList<>())
                .opciones(new ArrayList<>()).casosDePrueba(new ArrayList<>())
                .lenguajesPermitidos(new ArrayList<>()).build();

            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(preguntaRepository.save(argThat(p -> p.getPuntaje().compareTo(BigDecimal.ONE) == 0))).thenReturn(saved);
            when(cuestionarioRepository.save(any())).thenReturn(cuestionario);
            when(preguntaRepository.countByCuestionarioId(1L)).thenReturn(1);

            PreguntaResponse response = preguntaService.crear(1L, request);

            assertThat(response.puntaje()).isEqualByComparingTo(BigDecimal.ONE);
        }
    }

    @Nested
    @DisplayName("Duplicar pregunta")
    class DuplicarTests {

        @Test
        @DisplayName("duplica pregunta con opciones y casos de prueba")
        void duplicarConOpciones() {
            OpcionRespuesta op = OpcionRespuesta.builder()
                .id(1L).texto("Opcion A").esCorrecta(true).pregunta(preguntaUnica).build();
            preguntaUnica.setOpciones(new ArrayList<>(List.of(op)));

            Pregunta copia = Pregunta.builder()
                .id(30L).titulo("P Unica").tipoPregunta(TipoPregunta.OPCION_UNICA)
                .puntaje(new BigDecimal("5.00")).cuestionarios(new ArrayList<>(List.of(cuestionario)))
                .opciones(new ArrayList<>()).casosDePrueba(new ArrayList<>())
                .lenguajesPermitidos(new ArrayList<>()).build();

            when(preguntaRepository.findById(10L)).thenReturn(Optional.of(preguntaUnica));
            when(preguntaRepository.findById(30L)).thenReturn(Optional.of(copia));
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(preguntaRepository.save(any(Pregunta.class))).thenReturn(copia);
            when(cuestionarioRepository.save(any())).thenReturn(cuestionario);
            when(opcionRespuestaRepository.save(any(OpcionRespuesta.class)))
                .thenReturn(OpcionRespuesta.builder().id(2L).texto("Opcion A").esCorrecta(true).pregunta(copia).build());
            when(preguntaRepository.countByCuestionarioId(1L)).thenReturn(2);

            PreguntaResponse response = preguntaService.duplicar(10L, 1L);

            assertThat(response.id()).isEqualTo(30L);
            verify(opcionRespuestaRepository).save(any(OpcionRespuesta.class));
        }

        @Test
        @DisplayName("duplica pregunta CODIGO con casos de prueba")
        void duplicarConCasos() {
            CasoDePrueba caso = CasoDePrueba.builder()
                .id(1L).input("5").expectedOutput("25").pregunta(preguntaCodigo).build();
            preguntaCodigo.setCasosDePrueba(new ArrayList<>(List.of(caso)));

            Pregunta copia = Pregunta.builder()
                .id(31L).titulo("P Codigo").tipoPregunta(TipoPregunta.CODIGO)
                .puntaje(new BigDecimal("10.00")).cuestionarios(new ArrayList<>(List.of(cuestionario)))
                .opciones(new ArrayList<>()).casosDePrueba(new ArrayList<>())
                .lenguajesPermitidos(new ArrayList<>(List.of(LenguajeProgramacion.JAVA))).build();

            when(preguntaRepository.findById(20L)).thenReturn(Optional.of(preguntaCodigo));
            when(preguntaRepository.findById(31L)).thenReturn(Optional.of(copia));
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(preguntaRepository.save(any(Pregunta.class))).thenReturn(copia);
            when(cuestionarioRepository.save(any())).thenReturn(cuestionario);
            when(casoDePruebaRepository.save(any(CasoDePrueba.class)))
                .thenReturn(CasoDePrueba.builder().id(2L).input("5").expectedOutput("25").pregunta(copia).build());
            when(preguntaRepository.countByCuestionarioId(1L)).thenReturn(1);

            PreguntaResponse response = preguntaService.duplicar(20L, 1L);

            assertThat(response.id()).isEqualTo(31L);
            verify(casoDePruebaRepository).save(any(CasoDePrueba.class));
        }

        @Test
        @DisplayName("falla si pregunta original no existe")
        void preguntaNoExiste() {
            when(preguntaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> preguntaService.duplicar(99L, 1L))
                .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("falla si cuestionario destino no existe")
        void cuestionarioDestinoNoExiste() {
            when(preguntaRepository.findById(10L)).thenReturn(Optional.of(preguntaUnica));
            when(cuestionarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> preguntaService.duplicar(10L, 99L))
                .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Asociar / Desvincular")
    class AsociarDesvincularTests {

        @Test
        @DisplayName("asocia pregunta a cuestionario")
        void asociarExitoso() {
            when(preguntaRepository.findById(10L)).thenReturn(Optional.of(preguntaUnica));
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(cuestionarioRepository.save(any())).thenReturn(cuestionario);
            when(preguntaRepository.countByCuestionarioId(1L)).thenReturn(1);

            PreguntaResponse response = preguntaService.asociar(10L, 1L);

            assertThat(response.id()).isEqualTo(10L);
            assertThat(cuestionario.getPreguntas()).contains(preguntaUnica);
        }

        @Test
        @DisplayName("falla si pregunta ya esta asociada")
        void asociarDuplicada() {
            cuestionario.getPreguntas().add(preguntaUnica);
            when(preguntaRepository.findById(10L)).thenReturn(Optional.of(preguntaUnica));
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));

            assertThatThrownBy(() -> preguntaService.asociar(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya esta asociada");
        }

        @Test
        @DisplayName("desvincula pregunta de cuestionario")
        void desvincularExitoso() {
            cuestionario.getPreguntas().add(preguntaUnica);
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(cuestionarioRepository.save(any())).thenReturn(cuestionario);
            when(preguntaRepository.countByCuestionarioId(1L)).thenReturn(0);

            preguntaService.desvincular(10L, 1L);

            assertThat(cuestionario.getPreguntas()).doesNotContain(preguntaUnica);
        }
    }

    @Nested
    @DisplayName("Actualizar pregunta")
    class ActualizarTests {

        @Test
        @DisplayName("actualiza solo campos no nulos")
        void actualizarParcial() {
            when(preguntaRepository.findById(10L)).thenReturn(Optional.of(preguntaUnica));
            when(preguntaRepository.save(any(Pregunta.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdatePreguntaRequest request = new UpdatePreguntaRequest(
                "Nuevo titulo", null, null, null, null);

            PreguntaResponse response = preguntaService.actualizar(10L, request);

            assertThat(response.titulo()).isEqualTo("Nuevo titulo");
            assertThat(response.tipoPregunta()).isEqualTo(TipoPregunta.OPCION_UNICA);
        }

        @Test
        @DisplayName("falla si cambia tipo a CODIGO sin lenguajes")
        void actualizarTipoCodigoSinLenguajes() {
            when(preguntaRepository.findById(10L)).thenReturn(Optional.of(preguntaUnica));

            UpdatePreguntaRequest request = new UpdatePreguntaRequest(
                null, null, TipoPregunta.CODIGO, null, null);

            assertThatThrownBy(() -> preguntaService.actualizar(10L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("requieren al menos un lenguaje");
        }

        @Test
        @DisplayName("falla si no existe")
        void actualizarNoExiste() {
            when(preguntaRepository.findById(99L)).thenReturn(Optional.empty());

            UpdatePreguntaRequest request = new UpdatePreguntaRequest("T", null, null, null, null);

            assertThatThrownBy(() -> preguntaService.actualizar(99L, request))
                .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Eliminar pregunta")
    class EliminarTests {

        @Test
        @DisplayName("elimina pregunta y la desvincula de cuestionarios")
        void eliminarConDesvinculacion() {
            preguntaUnica.getCuestionarios().add(cuestionario);
            cuestionario.getPreguntas().add(preguntaUnica);

            when(preguntaRepository.findById(10L)).thenReturn(Optional.of(preguntaUnica));
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(cuestionarioRepository.save(any())).thenReturn(cuestionario);
            when(preguntaRepository.countByCuestionarioId(1L)).thenReturn(0);

            preguntaService.eliminar(10L);

            verify(preguntaRepository).delete(preguntaUnica);
            verify(cuestionarioRepository, atLeast(1)).save(cuestionario);
        }

        @Test
        @DisplayName("falla si pregunta no existe")
        void eliminarNoExiste() {
            when(preguntaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> preguntaService.eliminar(99L))
                .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Agregar opcion")
    class AgregarOpcionTests {

        @Test
        @DisplayName("agrega opcion a pregunta OPCION_UNICA")
        void agregarOpcionExitoso() {
            CreateOpcionRequest request = new CreateOpcionRequest("Opcion A", true);
            OpcionRespuesta saved = OpcionRespuesta.builder()
                .id(1L).texto("Opcion A").esCorrecta(true).pregunta(preguntaUnica).build();

            when(preguntaRepository.findById(10L)).thenReturn(Optional.of(preguntaUnica));
            when(opcionRespuestaRepository.save(any(OpcionRespuesta.class))).thenReturn(saved);

            OpcionRespuestaResponse response = preguntaService.agregarOpcion(10L, request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.texto()).isEqualTo("Opcion A");
            assertThat(response.esCorrecta()).isTrue();
        }

        @Test
        @DisplayName("falla si pregunta es tipo CODIGO")
        void agregarOpcionACodigo() {
            CreateOpcionRequest request = new CreateOpcionRequest("Opcion", false);
            when(preguntaRepository.findById(20L)).thenReturn(Optional.of(preguntaCodigo));

            assertThatThrownBy(() -> preguntaService.agregarOpcion(20L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No se pueden agregar opciones a preguntas de tipo CODIGO");
        }

        @Test
        @DisplayName("esCorrecta es false por defecto si es null")
        void esCorrectaDefaultFalse() {
            CreateOpcionRequest request = new CreateOpcionRequest("Opcion B", null);
            OpcionRespuesta saved = OpcionRespuesta.builder()
                .id(2L).texto("Opcion B").esCorrecta(false).pregunta(preguntaUnica).build();

            when(preguntaRepository.findById(10L)).thenReturn(Optional.of(preguntaUnica));
            when(opcionRespuestaRepository.save(argThat(o -> !o.getEsCorrecta()))).thenReturn(saved);

            OpcionRespuestaResponse response = preguntaService.agregarOpcion(10L, request);

            assertThat(response.esCorrecta()).isFalse();
        }
    }

    @Nested
    @DisplayName("Eliminar opcion")
    class EliminarOpcionTests {

        @Test
        @DisplayName("elimina opcion existente")
        void eliminarOpcionExitoso() {
            when(opcionRespuestaRepository.existsById(1L)).thenReturn(true);

            preguntaService.eliminarOpcion(1L);

            verify(opcionRespuestaRepository).deleteById(1L);
        }

        @Test
        @DisplayName("falla si opcion no existe")
        void eliminarOpcionNoExiste() {
            when(opcionRespuestaRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> preguntaService.eliminarOpcion(99L))
                .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Agregar caso de prueba")
    class AgregarCasoDePruebaTests {

        @Test
        @DisplayName("agrega caso de prueba a pregunta CODIGO")
        void agregarCasoExitoso() {
            CreateCasoDePruebaRequest request = new CreateCasoDePruebaRequest("5", "25");
            CasoDePrueba saved = CasoDePrueba.builder()
                .id(1L).input("5").expectedOutput("25").pregunta(preguntaCodigo).build();

            when(preguntaRepository.findById(20L)).thenReturn(Optional.of(preguntaCodigo));
            when(casoDePruebaRepository.save(any(CasoDePrueba.class))).thenReturn(saved);

            CasoDePruebaResponse response = preguntaService.agregarCasoDePrueba(20L, request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.input()).isEqualTo("5");
            assertThat(response.expectedOutput()).isEqualTo("25");
        }

        @Test
        @DisplayName("falla si pregunta no es tipo CODIGO")
        void agregarCasoANoCodigO() {
            CreateCasoDePruebaRequest request = new CreateCasoDePruebaRequest("5", "25");
            when(preguntaRepository.findById(10L)).thenReturn(Optional.of(preguntaUnica));

            assertThatThrownBy(() -> preguntaService.agregarCasoDePrueba(10L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Solo se pueden agregar casos de prueba a preguntas de tipo CODIGO");
        }
    }

    @Nested
    @DisplayName("Eliminar caso de prueba")
    class EliminarCasoDePruebaTests {

        @Test
        @DisplayName("elimina caso de prueba existente")
        void eliminarCasoExitoso() {
            when(casoDePruebaRepository.existsById(1L)).thenReturn(true);

            preguntaService.eliminarCasoDePrueba(1L);

            verify(casoDePruebaRepository).deleteById(1L);
        }

        @Test
        @DisplayName("falla si caso de prueba no existe")
        void eliminarCasoNoExiste() {
            when(casoDePruebaRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> preguntaService.eliminarCasoDePrueba(99L))
                .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Buscar y listar")
    class BuscarListarTests {

        @Test
        @DisplayName("buscarPorId retorna response")
        void buscarExitoso() {
            when(preguntaRepository.findById(10L)).thenReturn(Optional.of(preguntaUnica));

            PreguntaResponse response = preguntaService.buscarPorId(10L);

            assertThat(response.id()).isEqualTo(10L);
        }

        @Test
        @DisplayName("buscarPorId falla si no existe")
        void buscarNoExiste() {
            when(preguntaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> preguntaService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("listarPorCuestionario retorna preguntas del cuestionario")
        void listarPorCuestionario() {
            when(cuestionarioRepository.existsById(1L)).thenReturn(true);
            when(preguntaRepository.findByCuestionarioId(1L)).thenReturn(List.of(preguntaUnica));

            List<PreguntaResponse> result = preguntaService.listarPorCuestionario(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("listarPorCuestionario falla si cuestionario no existe")
        void listarPorCuestionarioNoExiste() {
            when(cuestionarioRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> preguntaService.listarPorCuestionario(99L))
                .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("listarTodas retorna todas las preguntas")
        void listarTodas() {
            when(preguntaRepository.findAll()).thenReturn(List.of(preguntaUnica, preguntaCodigo));

            List<PreguntaResponse> result = preguntaService.listarTodas();

            assertThat(result).hasSize(2);
        }
    }
}
