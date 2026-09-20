package com.assessment.mngr.service;

import com.assessment.mngr.controller.dto.IniciarIntentoRequest;
import com.assessment.mngr.controller.dto.IntentoExamenResponse;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntentoExamenServiceTest {

    @Mock
    private IntentoExamenRepository intentoExamenRepository;
    @Mock
    private CuestionarioRepository cuestionarioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PreguntaRepository preguntaRepository;
    @Mock
    private RespuestaCandidatoRepository respuestaCandidatoRepository;
    @Mock
    private CalificacionService calificacionService;
    @Mock
    private AsignacionCuestionarioService asignacionCuestionarioService;
    @Mock
    private PreguntaService preguntaService;

    @InjectMocks
    private IntentoExamenService intentoExamenService;

    private Usuario candidato;
    private Cuestionario cuestionario;
    private Pregunta pregunta;

    @BeforeEach
    void setUp() {
        candidato = Usuario.builder()
            .id(10L)
            .username("candidato1")
            .nombreCompleto("Candidato Uno")
            .email("c1@test.com")
            .password("hashed")
            .build();

        cuestionario = Cuestionario.builder()
            .id(1L)
            .nombre("Java Quiz")
            .tiempoLimite(60)
            .cantidadPreguntas(1)
            .maxIntentos(3)
            .activo(true)
            .creadoPor(Usuario.builder().id(99L).username("admin").nombreCompleto("Admin").build())
            .createdAt(LocalDateTime.now())
            .build();

        pregunta = Pregunta.builder()
            .id(1L)
            .titulo("Pregunta 1")
            .tipoPregunta(TipoPregunta.OPCION_UNICA)
            .puntaje(new BigDecimal("10.00"))
            .build();
    }

    @Nested
    @DisplayName("Iniciar intento")
    class IniciarIntentoTests {

        @Test
        @DisplayName("inicia intento exitosamente con snapshot de preguntas")
        void iniciarExitoso() {
            IniciarIntentoRequest request = new IniciarIntentoRequest(1L);
            when(usuarioRepository.findByUsername("candidato1")).thenReturn(Optional.of(candidato));
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            doNothing().when(asignacionCuestionarioService).validarAsignacionActiva(10L, 1L);
            when(intentoExamenRepository.findByCandidatoIdAndCuestionarioId(10L, 1L)).thenReturn(List.of());
            when(preguntaRepository.findByCuestionarioId(1L)).thenReturn(List.of(pregunta));
            when(intentoExamenRepository.save(any(IntentoExamen.class))).thenAnswer(inv -> {
                IntentoExamen i = inv.getArgument(0);
                i.setId(100L);
                return i;
            });
            when(respuestaCandidatoRepository.saveAll(anyList())).thenReturn(List.of());

            IntentoExamenResponse response = intentoExamenService.iniciarIntento(request, "candidato1");

            assertThat(response.id()).isEqualTo(100L);
            assertThat(response.candidatoId()).isEqualTo(10L);
            assertThat(response.cuestionarioId()).isEqualTo(1L);
            assertThat(response.estado()).isEqualTo(EstadoIntento.EN_PROGRESO);
            assertThat(response.puntajeMaximo()).isEqualByComparingTo(new BigDecimal("10.00"));

            verify(respuestaCandidatoRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("lanza excepcion si usuario no existe")
        void usuarioNoExiste() {
            IniciarIntentoRequest request = new IniciarIntentoRequest(1L);
            when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> intentoExamenService.iniciarIntento(request, "fantasma"))
                .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("lanza excepcion si cuestionario no existe")
        void cuestionarioNoExiste() {
            IniciarIntentoRequest request = new IniciarIntentoRequest(999L);
            when(usuarioRepository.findByUsername("candidato1")).thenReturn(Optional.of(candidato));
            when(cuestionarioRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> intentoExamenService.iniciarIntento(request, "candidato1"))
                .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("lanza excepcion si cuestionario no esta activo")
        void cuestionarioInactivo() {
            cuestionario.setActivo(false);
            IniciarIntentoRequest request = new IniciarIntentoRequest(1L);
            when(usuarioRepository.findByUsername("candidato1")).thenReturn(Optional.of(candidato));
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));

            assertThatThrownBy(() -> intentoExamenService.iniciarIntento(request, "candidato1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no esta activo");
        }

        @Test
        @DisplayName("lanza excepcion si ya tiene intento en progreso")
        void intentoEnProgreso() {
            IniciarIntentoRequest request = new IniciarIntentoRequest(1L);
            when(usuarioRepository.findByUsername("candidato1")).thenReturn(Optional.of(candidato));
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            doNothing().when(asignacionCuestionarioService).validarAsignacionActiva(10L, 1L);

            IntentoExamen enProgreso = IntentoExamen.builder()
                .id(50L).estado(EstadoIntento.EN_PROGRESO)
                .candidato(candidato).cuestionario(cuestionario)
                .build();
            when(intentoExamenRepository.findByCandidatoIdAndCuestionarioId(10L, 1L))
                .thenReturn(List.of(enProgreso));

            assertThatThrownBy(() -> intentoExamenService.iniciarIntento(request, "candidato1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("intento en progreso");
        }

        @Test
        @DisplayName("lanza excepcion si alcanzo maximo de intentos")
        void maximoIntentos() {
            cuestionario.setMaxIntentos(1);
            IniciarIntentoRequest request = new IniciarIntentoRequest(1L);
            when(usuarioRepository.findByUsername("candidato1")).thenReturn(Optional.of(candidato));
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            doNothing().when(asignacionCuestionarioService).validarAsignacionActiva(10L, 1L);

            IntentoExamen finalizado = IntentoExamen.builder()
                .id(50L).estado(EstadoIntento.FINALIZADO)
                .candidato(candidato).cuestionario(cuestionario)
                .build();
            when(intentoExamenRepository.findByCandidatoIdAndCuestionarioId(10L, 1L))
                .thenReturn(List.of(finalizado));

            assertThatThrownBy(() -> intentoExamenService.iniciarIntento(request, "candidato1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("maximo de intentos");
        }
    }

    @Nested
    @DisplayName("Finalizar intento")
    class FinalizarIntentoTests {

        @Test
        @DisplayName("finaliza intento y calcula puntaje total")
        void finalizarExitoso() {
            IntentoExamen intento = IntentoExamen.builder()
                .id(100L)
                .candidato(candidato)
                .cuestionario(cuestionario)
                .fechaInicio(LocalDateTime.now().minusMinutes(30))
                .estado(EstadoIntento.EN_PROGRESO)
                .puntajeMaximo(new BigDecimal("20.00"))
                .build();

            RespuestaCandidato r1 = RespuestaCandidato.builder()
                .id(1L).pregunta(pregunta).intentoExamen(intento)
                .esCorrecta(true).puntajeObtenido(new BigDecimal("10.00"))
                .build();
            RespuestaCandidato r2 = RespuestaCandidato.builder()
                .id(2L).pregunta(Pregunta.builder().id(2L).titulo("P2").puntaje(new BigDecimal("10.00")).build())
                .intentoExamen(intento)
                .esCorrecta(false).puntajeObtenido(BigDecimal.ZERO)
                .build();

            when(intentoExamenRepository.findById(100L)).thenReturn(Optional.of(intento));
            when(respuestaCandidatoRepository.findByIntentoExamenId(100L)).thenReturn(List.of(r1, r2));
            when(intentoExamenRepository.save(any(IntentoExamen.class))).thenAnswer(inv -> inv.getArgument(0));

            var response = intentoExamenService.finalizarIntento(100L, "candidato1");

            assertThat(response.estado()).isEqualTo(EstadoIntento.FINALIZADO);
            assertThat(response.puntajeTotal()).isEqualByComparingTo(new BigDecimal("10.00"));
            assertThat(intento.getFechaFin()).isNotNull();
            assertThat(intento.getTiempoConsumido()).isGreaterThan(0);
        }

        @Test
        @DisplayName("lanza excepcion si el intento no pertenece al usuario")
        void intentoDeOtroUsuario() {
            Usuario otro = Usuario.builder().id(99L).username("otro").nombreCompleto("Otro").build();
            IntentoExamen intento = IntentoExamen.builder()
                .id(100L).candidato(otro).cuestionario(cuestionario)
                .estado(EstadoIntento.EN_PROGRESO)
                .fechaInicio(LocalDateTime.now())
                .build();

            when(intentoExamenRepository.findById(100L)).thenReturn(Optional.of(intento));

            assertThatThrownBy(() -> intentoExamenService.finalizarIntento(100L, "candidato1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no pertenece");
        }

        @Test
        @DisplayName("lanza excepcion si el intento ya fue finalizado")
        void intentoYaFinalizado() {
            IntentoExamen intento = IntentoExamen.builder()
                .id(100L).candidato(candidato).cuestionario(cuestionario)
                .estado(EstadoIntento.FINALIZADO)
                .fechaInicio(LocalDateTime.now())
                .build();

            when(intentoExamenRepository.findById(100L)).thenReturn(Optional.of(intento));

            assertThatThrownBy(() -> intentoExamenService.finalizarIntento(100L, "candidato1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya fue finalizado");
        }
    }

    @Nested
    @DisplayName("Abandonar intento")
    class AbandonarIntentoTests {

        @Test
        @DisplayName("abandona intento con puntaje cero")
        void abandonarExitoso() {
            IntentoExamen intento = IntentoExamen.builder()
                .id(100L).candidato(candidato).cuestionario(cuestionario)
                .estado(EstadoIntento.EN_PROGRESO)
                .fechaInicio(LocalDateTime.now().minusMinutes(5))
                .build();

            when(intentoExamenRepository.findById(100L)).thenReturn(Optional.of(intento));

            intentoExamenService.abandonarIntento(100L, "candidato1");

            assertThat(intento.getEstado()).isEqualTo(EstadoIntento.ABANDONADO);
            assertThat(intento.getPuntajeTotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(intento.getFechaFin()).isNotNull();
            assertThat(intento.getTiempoConsumido()).isGreaterThan(0);
            verify(intentoExamenRepository).save(intento);
        }

        @Test
        @DisplayName("lanza excepcion si intento ya fue finalizado")
        void noSePuedeAbandonarFinalizado() {
            IntentoExamen intento = IntentoExamen.builder()
                .id(100L).candidato(candidato).cuestionario(cuestionario)
                .estado(EstadoIntento.FINALIZADO)
                .fechaInicio(LocalDateTime.now())
                .build();

            when(intentoExamenRepository.findById(100L)).thenReturn(Optional.of(intento));

            assertThatThrownBy(() -> intentoExamenService.abandonarIntento(100L, "candidato1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("en progreso");
        }

        @Test
        @DisplayName("lanza excepcion si intento no pertenece al usuario")
        void intentoDeOtroUsuario() {
            Usuario otro = Usuario.builder().id(99L).username("otro").nombreCompleto("Otro").build();
            IntentoExamen intento = IntentoExamen.builder()
                .id(100L).candidato(otro).cuestionario(cuestionario)
                .estado(EstadoIntento.EN_PROGRESO)
                .fechaInicio(LocalDateTime.now())
                .build();

            when(intentoExamenRepository.findById(100L)).thenReturn(Optional.of(intento));

            assertThatThrownBy(() -> intentoExamenService.abandonarIntento(100L, "candidato1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no pertenece");
        }
    }

    @Nested
    @DisplayName("Buscar por id")
    class BuscarTests {

        @Test
        @DisplayName("buscarPorId retorna response")
        void buscarExitoso() {
            IntentoExamen intento = IntentoExamen.builder()
                .id(100L).candidato(candidato).cuestionario(cuestionario)
                .estado(EstadoIntento.EN_PROGRESO)
                .fechaInicio(LocalDateTime.now())
                .build();
            when(intentoExamenRepository.findById(100L)).thenReturn(Optional.of(intento));

            IntentoExamenResponse response = intentoExamenService.buscarPorId(100L);

            assertThat(response.id()).isEqualTo(100L);
            assertThat(response.candidatoNombre()).isEqualTo("Candidato Uno");
        }

        @Test
        @DisplayName("buscarPorId lanza excepcion si no existe")
        void buscarNoExiste() {
            when(intentoExamenRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> intentoExamenService.buscarPorId(999L))
                .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
