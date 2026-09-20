package com.assessment.mngr.service;

import com.assessment.mngr.controller.dto.AsignacionBatchRequest;
import com.assessment.mngr.controller.dto.AsignacionResponse;
import com.assessment.mngr.controller.dto.CreateAsignacionRequest;
import com.assessment.mngr.exception.BusinessException;
import com.assessment.mngr.exception.DuplicateEntityException;
import com.assessment.mngr.exception.EntityNotFoundException;
import com.assessment.mngr.model.*;
import com.assessment.mngr.repository.AsignacionCuestionarioRepository;
import com.assessment.mngr.repository.CuestionarioRepository;
import com.assessment.mngr.repository.IntentoExamenRepository;
import com.assessment.mngr.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionCuestionarioServiceTest {

    @Mock
    private AsignacionCuestionarioRepository asignacionRepository;
    @Mock
    private CuestionarioRepository cuestionarioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private IntentoExamenRepository intentoExamenRepository;

    @InjectMocks
    private AsignacionCuestionarioService asignacionService;

    private Usuario admin;
    private Usuario candidato;
    private Usuario candidato2;
    private Cuestionario cuestionario;
    private LocalDateTime desde;
    private LocalDateTime hasta;

    @BeforeEach
    void setUp() {
        admin = Usuario.builder()
            .id(1L).username("admin").nombreCompleto("Admin User").email("admin@t.com").password("h").build();
        candidato = Usuario.builder()
            .id(10L).username("candidato1").nombreCompleto("Candidato Uno").email("c1@t.com").password("h").build();
        candidato2 = Usuario.builder()
            .id(11L).username("candidato2").nombreCompleto("Candidato Dos").email("c2@t.com").password("h").build();

        cuestionario = Cuestionario.builder()
            .id(1L).nombre("Quiz").tiempoLimite(60).cantidadPreguntas(5)
            .maxIntentos(3).activo(true).creadoPor(admin).build();

        desde = LocalDateTime.now().plusDays(1);
        hasta = LocalDateTime.now().plusDays(7);
    }

    @Nested
    @DisplayName("Crear asignacion")
    class CrearTests {

        @Test
        @DisplayName("crea asignacion exitosamente")
        void crearExitoso() {
            CreateAsignacionRequest request = new CreateAsignacionRequest(1L, 10L, desde, hasta);

            AsignacionCuestionario saved = AsignacionCuestionario.builder()
                .id(100L).cuestionario(cuestionario).candidato(candidato)
                .asignadoPor(admin).disponibleDesde(desde).disponibleHasta(hasta).build();

            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(usuarioRepository.findById(10L)).thenReturn(Optional.of(candidato));
            when(asignacionRepository.existsByCuestionarioIdAndCandidatoId(1L, 10L)).thenReturn(false);
            when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
            when(asignacionRepository.save(any(AsignacionCuestionario.class))).thenReturn(saved);
            when(intentoExamenRepository.findByCandidatoIdAndCuestionarioId(10L, 1L)).thenReturn(List.of());

            AsignacionResponse response = asignacionService.crear(request, "admin");

            assertThat(response.id()).isEqualTo(100L);
            assertThat(response.cuestionarioId()).isEqualTo(1L);
            assertThat(response.candidatoId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("falla si disponibleHasta no es posterior a disponibleDesde")
        void fechasInvalidas() {
            LocalDateTime ahora = LocalDateTime.now();
            CreateAsignacionRequest request = new CreateAsignacionRequest(1L, 10L, ahora, ahora.minusDays(1));

            assertThatThrownBy(() -> asignacionService.crear(request, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("disponibleHasta");
        }

        @Test
        @DisplayName("falla si fechas son iguales")
        void fechasIguales() {
            LocalDateTime fecha = LocalDateTime.now().plusDays(1);
            CreateAsignacionRequest request = new CreateAsignacionRequest(1L, 10L, fecha, fecha);

            assertThatThrownBy(() -> asignacionService.crear(request, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("disponibleHasta");
        }

        @Test
        @DisplayName("falla si cuestionario no existe")
        void cuestionarioNoExiste() {
            CreateAsignacionRequest request = new CreateAsignacionRequest(99L, 10L, desde, hasta);
            when(cuestionarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> asignacionService.crear(request, "admin"))
                .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("falla si candidato no existe")
        void candidatoNoExiste() {
            CreateAsignacionRequest request = new CreateAsignacionRequest(1L, 99L, desde, hasta);
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> asignacionService.crear(request, "admin"))
                .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("falla si asignacion ya existe (duplicado)")
        void asignacionDuplicada() {
            CreateAsignacionRequest request = new CreateAsignacionRequest(1L, 10L, desde, hasta);
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(usuarioRepository.findById(10L)).thenReturn(Optional.of(candidato));
            when(asignacionRepository.existsByCuestionarioIdAndCandidatoId(1L, 10L)).thenReturn(true);

            assertThatThrownBy(() -> asignacionService.crear(request, "admin"))
                .isInstanceOf(DuplicateEntityException.class);
        }
    }

    @Nested
    @DisplayName("Crear batch")
    class CrearBatchTests {

        @Test
        @DisplayName("crea batch de asignaciones exitosamente")
        void batchExitoso() {
            AsignacionBatchRequest request = new AsignacionBatchRequest(1L, List.of(10L, 11L), desde, hasta);

            AsignacionCuestionario a1 = AsignacionCuestionario.builder()
                .id(100L).cuestionario(cuestionario).candidato(candidato)
                .asignadoPor(admin).disponibleDesde(desde).disponibleHasta(hasta).build();
            AsignacionCuestionario a2 = AsignacionCuestionario.builder()
                .id(101L).cuestionario(cuestionario).candidato(candidato2)
                .asignadoPor(admin).disponibleDesde(desde).disponibleHasta(hasta).build();

            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(usuarioRepository.findById(10L)).thenReturn(Optional.of(candidato));
            when(usuarioRepository.findById(11L)).thenReturn(Optional.of(candidato2));
            when(asignacionRepository.existsByCuestionarioIdAndCandidatoId(1L, 10L)).thenReturn(false);
            when(asignacionRepository.existsByCuestionarioIdAndCandidatoId(1L, 11L)).thenReturn(false);
            when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
            when(asignacionRepository.save(any(AsignacionCuestionario.class))).thenReturn(a1, a2);
            when(intentoExamenRepository.findByCandidatoIdAndCuestionarioId(anyLong(), eq(1L))).thenReturn(List.of());

            List<AsignacionResponse> result = asignacionService.crearBatch(request, "admin");

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("batch omite duplicados sin fallar")
        void batchConDuplicados() {
            AsignacionBatchRequest request = new AsignacionBatchRequest(1L, List.of(10L, 11L), desde, hasta);

            AsignacionCuestionario a2 = AsignacionCuestionario.builder()
                .id(101L).cuestionario(cuestionario).candidato(candidato2)
                .asignadoPor(admin).disponibleDesde(desde).disponibleHasta(hasta).build();

            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(usuarioRepository.findById(10L)).thenReturn(Optional.of(candidato));
            when(usuarioRepository.findById(11L)).thenReturn(Optional.of(candidato2));
            // Primer candidato ya tiene asignacion
            when(asignacionRepository.existsByCuestionarioIdAndCandidatoId(1L, 10L)).thenReturn(true);
            when(asignacionRepository.existsByCuestionarioIdAndCandidatoId(1L, 11L)).thenReturn(false);
            when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
            when(asignacionRepository.save(any(AsignacionCuestionario.class))).thenReturn(a2);
            when(intentoExamenRepository.findByCandidatoIdAndCuestionarioId(11L, 1L)).thenReturn(List.of());

            List<AsignacionResponse> result = asignacionService.crearBatch(request, "admin");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("batch falla si fechas invalidas")
        void batchFechasInvalidas() {
            LocalDateTime ahora = LocalDateTime.now();
            AsignacionBatchRequest request = new AsignacionBatchRequest(1L, List.of(10L), ahora, ahora.minusDays(1));

            assertThatThrownBy(() -> asignacionService.crearBatch(request, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("disponibleHasta");
        }
    }

    @Nested
    @DisplayName("Eliminar asignacion")
    class EliminarTests {

        @Test
        @DisplayName("elimina asignacion existente")
        void eliminarExitoso() {
            AsignacionCuestionario asignacion = AsignacionCuestionario.builder()
                .id(100L).cuestionario(cuestionario).candidato(candidato).asignadoPor(admin).build();
            when(asignacionRepository.findById(100L)).thenReturn(Optional.of(asignacion));

            asignacionService.eliminar(100L);

            verify(asignacionRepository).delete(asignacion);
        }

        @Test
        @DisplayName("falla si asignacion no existe")
        void eliminarNoExiste() {
            when(asignacionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> asignacionService.eliminar(99L))
                .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Validar asignacion activa")
    class ValidarTests {

        @Test
        @DisplayName("valida exitosamente si estamos dentro de la ventana")
        void validarExitoso() {
            AsignacionCuestionario asignacion = AsignacionCuestionario.builder()
                .id(100L).cuestionario(cuestionario).candidato(candidato)
                .disponibleDesde(LocalDateTime.now().minusDays(1))
                .disponibleHasta(LocalDateTime.now().plusDays(1))
                .build();
            when(asignacionRepository.findByCuestionarioIdAndCandidatoId(1L, 10L))
                .thenReturn(Optional.of(asignacion));

            asignacionService.validarAsignacionActiva(10L, 1L);

            // No exception = success
        }

        @Test
        @DisplayName("falla si no existe asignacion")
        void sinAsignacion() {
            when(asignacionRepository.findByCuestionarioIdAndCandidatoId(1L, 10L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> asignacionService.validarAsignacionActiva(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No tiene una asignacion activa");
        }

        @Test
        @DisplayName("falla si aun no esta disponible")
        void noDisponibleAun() {
            AsignacionCuestionario asignacion = AsignacionCuestionario.builder()
                .id(100L).cuestionario(cuestionario).candidato(candidato)
                .disponibleDesde(LocalDateTime.now().plusDays(5))
                .disponibleHasta(LocalDateTime.now().plusDays(10))
                .build();
            when(asignacionRepository.findByCuestionarioIdAndCandidatoId(1L, 10L))
                .thenReturn(Optional.of(asignacion));

            assertThatThrownBy(() -> asignacionService.validarAsignacionActiva(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("aun no esta disponible");
        }

        @Test
        @DisplayName("falla si ventana ya expiro")
        void ventanaExpirada() {
            AsignacionCuestionario asignacion = AsignacionCuestionario.builder()
                .id(100L).cuestionario(cuestionario).candidato(candidato)
                .disponibleDesde(LocalDateTime.now().minusDays(10))
                .disponibleHasta(LocalDateTime.now().minusDays(1))
                .build();
            when(asignacionRepository.findByCuestionarioIdAndCandidatoId(1L, 10L))
                .thenReturn(Optional.of(asignacion));

            assertThatThrownBy(() -> asignacionService.validarAsignacionActiva(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ha expirado");
        }
    }

    @Nested
    @DisplayName("Listar asignaciones")
    class ListarTests {

        @Test
        @DisplayName("listarPorCuestionario retorna asignaciones")
        void listarPorCuestionario() {
            AsignacionCuestionario asignacion = AsignacionCuestionario.builder()
                .id(100L).cuestionario(cuestionario).candidato(candidato)
                .asignadoPor(admin).disponibleDesde(desde).disponibleHasta(hasta).build();

            when(cuestionarioRepository.existsById(1L)).thenReturn(true);
            when(asignacionRepository.findByCuestionarioId(1L)).thenReturn(List.of(asignacion));
            when(intentoExamenRepository.findByCandidatoIdAndCuestionarioId(10L, 1L)).thenReturn(List.of());

            List<AsignacionResponse> result = asignacionService.listarPorCuestionario(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).estado()).isEqualTo("PENDIENTE");
        }

        @Test
        @DisplayName("listarPorCuestionario falla si cuestionario no existe")
        void listarPorCuestionarioNoExiste() {
            when(cuestionarioRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> asignacionService.listarPorCuestionario(99L))
                .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("listarAsignacionesCandidato retorna sus asignaciones")
        void listarPorCandidato() {
            AsignacionCuestionario asignacion = AsignacionCuestionario.builder()
                .id(100L).cuestionario(cuestionario).candidato(candidato)
                .asignadoPor(admin).disponibleDesde(desde).disponibleHasta(hasta).build();

            when(usuarioRepository.findByUsername("candidato1")).thenReturn(Optional.of(candidato));
            when(asignacionRepository.findByCandidatoId(10L)).thenReturn(List.of(asignacion));
            when(intentoExamenRepository.findByCandidatoIdAndCuestionarioId(10L, 1L)).thenReturn(List.of());

            List<AsignacionResponse> result = asignacionService.listarAsignacionesCandidato("candidato1");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("listarAsignacionesCandidato falla si usuario no existe")
        void listarPorCandidatoNoExiste() {
            when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> asignacionService.listarAsignacionesCandidato("fantasma"))
                .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("estado INACTIVA cuando cuestionario no esta activo")
        void estadoInactiva() {
            cuestionario.setActivo(false);
            AsignacionCuestionario asignacion = AsignacionCuestionario.builder()
                .id(100L).cuestionario(cuestionario).candidato(candidato)
                .asignadoPor(admin).disponibleDesde(desde).disponibleHasta(hasta).build();

            when(cuestionarioRepository.existsById(1L)).thenReturn(true);
            when(asignacionRepository.findByCuestionarioId(1L)).thenReturn(List.of(asignacion));
            when(intentoExamenRepository.findByCandidatoIdAndCuestionarioId(10L, 1L)).thenReturn(List.of());

            List<AsignacionResponse> result = asignacionService.listarPorCuestionario(1L);

            assertThat(result.get(0).estado()).isEqualTo("INACTIVA");
        }

        @Test
        @DisplayName("estado ACTIVA dentro de la ventana")
        void estadoActiva() {
            AsignacionCuestionario asignacion = AsignacionCuestionario.builder()
                .id(100L).cuestionario(cuestionario).candidato(candidato)
                .asignadoPor(admin)
                .disponibleDesde(LocalDateTime.now().minusDays(1))
                .disponibleHasta(LocalDateTime.now().plusDays(1))
                .build();

            when(cuestionarioRepository.existsById(1L)).thenReturn(true);
            when(asignacionRepository.findByCuestionarioId(1L)).thenReturn(List.of(asignacion));
            when(intentoExamenRepository.findByCandidatoIdAndCuestionarioId(10L, 1L)).thenReturn(List.of());

            List<AsignacionResponse> result = asignacionService.listarPorCuestionario(1L);

            assertThat(result.get(0).estado()).isEqualTo("ACTIVA");
        }

        @Test
        @DisplayName("estado EXPIRADA despues de la ventana")
        void estadoExpirada() {
            AsignacionCuestionario asignacion = AsignacionCuestionario.builder()
                .id(100L).cuestionario(cuestionario).candidato(candidato)
                .asignadoPor(admin)
                .disponibleDesde(LocalDateTime.now().minusDays(10))
                .disponibleHasta(LocalDateTime.now().minusDays(1))
                .build();

            when(cuestionarioRepository.existsById(1L)).thenReturn(true);
            when(asignacionRepository.findByCuestionarioId(1L)).thenReturn(List.of(asignacion));
            when(intentoExamenRepository.findByCandidatoIdAndCuestionarioId(10L, 1L)).thenReturn(List.of());

            List<AsignacionResponse> result = asignacionService.listarPorCuestionario(1L);

            assertThat(result.get(0).estado()).isEqualTo("EXPIRADA");
        }

        @Test
        @DisplayName("intentosUsados cuenta solo FINALIZADOS y ABANDONADOS")
        void intentosUsadosCorrectamente() {
            AsignacionCuestionario asignacion = AsignacionCuestionario.builder()
                .id(100L).cuestionario(cuestionario).candidato(candidato)
                .asignadoPor(admin)
                .disponibleDesde(LocalDateTime.now().minusDays(1))
                .disponibleHasta(LocalDateTime.now().plusDays(1))
                .build();

            IntentoExamen finalizado = IntentoExamen.builder()
                .id(1L).estado(EstadoIntento.FINALIZADO).candidato(candidato).cuestionario(cuestionario).build();
            IntentoExamen abandonado = IntentoExamen.builder()
                .id(2L).estado(EstadoIntento.ABANDONADO).candidato(candidato).cuestionario(cuestionario).build();
            IntentoExamen enProgreso = IntentoExamen.builder()
                .id(3L).estado(EstadoIntento.EN_PROGRESO).candidato(candidato).cuestionario(cuestionario).build();

            when(cuestionarioRepository.existsById(1L)).thenReturn(true);
            when(asignacionRepository.findByCuestionarioId(1L)).thenReturn(List.of(asignacion));
            when(intentoExamenRepository.findByCandidatoIdAndCuestionarioId(10L, 1L))
                .thenReturn(List.of(finalizado, abandonado, enProgreso));

            List<AsignacionResponse> result = asignacionService.listarPorCuestionario(1L);

            assertThat(result.get(0).intentosUsados()).isEqualTo(2);
        }
    }
}
