package com.assessment.mngr.service;

import com.assessment.mngr.controller.dto.CreateCuestionarioRequest;
import com.assessment.mngr.controller.dto.CuestionarioResponse;
import com.assessment.mngr.controller.dto.UpdateCuestionarioRequest;
import com.assessment.mngr.exception.EntityNotFoundException;
import com.assessment.mngr.model.Cuestionario;
import com.assessment.mngr.model.Usuario;
import com.assessment.mngr.repository.CuestionarioRepository;
import com.assessment.mngr.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CuestionarioServiceTest {

    @Mock
    private CuestionarioRepository cuestionarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CuestionarioService cuestionarioService;

    private Usuario admin;
    private Cuestionario cuestionario;

    @BeforeEach
    void setUp() {
        admin = Usuario.builder()
            .id(1L)
            .username("admin")
            .nombreCompleto("Admin User")
            .email("admin@test.com")
            .password("hashed")
            .build();

        cuestionario = Cuestionario.builder()
            .id(1L)
            .nombre("Java Basics")
            .descripcion("Cuestionario de fundamentos Java")
            .tiempoLimite(60)
            .cantidadPreguntas(0)
            .maxIntentos(3)
            .activo(true)
            .creadoPor(admin)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("Crear cuestionario")
    class CrearTests {

        @Test
        @DisplayName("crea cuestionario con datos validos")
        void crearExitoso() {
            CreateCuestionarioRequest request = new CreateCuestionarioRequest(
                "Java Basics", "Fundamentos", 60, 3
            );
            when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
            when(cuestionarioRepository.save(any(Cuestionario.class))).thenReturn(cuestionario);

            CuestionarioResponse response = cuestionarioService.crear(request, "admin");

            assertThat(response.nombre()).isEqualTo("Java Basics");
            assertThat(response.tiempoLimite()).isEqualTo(60);
            assertThat(response.maxIntentos()).isEqualTo(3);
            assertThat(response.creadoPor()).isEqualTo("admin");

            ArgumentCaptor<Cuestionario> captor = ArgumentCaptor.forClass(Cuestionario.class);
            verify(cuestionarioRepository).save(captor.capture());
            Cuestionario saved = captor.getValue();
            assertThat(saved.getNombre()).isEqualTo("Java Basics");
            assertThat(saved.getCreadoPor()).isEqualTo(admin);
        }

        @Test
        @DisplayName("lanza EntityNotFoundException si usuario no existe")
        void usuarioNoExiste() {
            CreateCuestionarioRequest request = new CreateCuestionarioRequest(
                "Test", "Desc", 30, 1
            );
            when(usuarioRepository.findByUsername("desconocido")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cuestionarioService.crear(request, "desconocido"))
                .isInstanceOf(EntityNotFoundException.class);

            verify(cuestionarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Buscar cuestionario")
    class BuscarTests {

        @Test
        @DisplayName("retorna cuestionario por id")
        void buscarExitoso() {
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));

            CuestionarioResponse response = cuestionarioService.buscarPorId(1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.nombre()).isEqualTo("Java Basics");
        }

        @Test
        @DisplayName("lanza EntityNotFoundException si no existe")
        void buscarNoExiste() {
            when(cuestionarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cuestionarioService.buscarPorId(99L))
                .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Listar cuestionarios")
    class ListarTests {

        @Test
        @DisplayName("listarTodos retorna todos los cuestionarios")
        void listarTodos() {
            Cuestionario inactivo = Cuestionario.builder()
                .id(2L).nombre("Inactivo").tiempoLimite(30).cantidadPreguntas(0)
                .maxIntentos(1).activo(false).creadoPor(admin).createdAt(LocalDateTime.now())
                .build();
            when(cuestionarioRepository.findAll()).thenReturn(List.of(cuestionario, inactivo));

            List<CuestionarioResponse> result = cuestionarioService.listarTodos();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("listarActivos retorna solo cuestionarios activos")
        void listarActivos() {
            when(cuestionarioRepository.findByActivoTrue()).thenReturn(List.of(cuestionario));

            List<CuestionarioResponse> result = cuestionarioService.listarActivos();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).activo()).isTrue();
        }
    }

    @Nested
    @DisplayName("Actualizar cuestionario")
    class ActualizarTests {

        @Test
        @DisplayName("actualiza solo campos no nulos")
        void actualizarParcial() {
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(cuestionarioRepository.save(any(Cuestionario.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateCuestionarioRequest request = new UpdateCuestionarioRequest(
                "Nuevo nombre", null, null, null, null
            );

            CuestionarioResponse response = cuestionarioService.actualizar(1L, request);

            assertThat(response.nombre()).isEqualTo("Nuevo nombre");
            assertThat(response.descripcion()).isEqualTo("Cuestionario de fundamentos Java");
            assertThat(response.tiempoLimite()).isEqualTo(60);
        }

        @Test
        @DisplayName("actualiza todos los campos cuando se proveen")
        void actualizarCompleto() {
            when(cuestionarioRepository.findById(1L)).thenReturn(Optional.of(cuestionario));
            when(cuestionarioRepository.save(any(Cuestionario.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateCuestionarioRequest request = new UpdateCuestionarioRequest(
                "Updated", "Nueva desc", 90, 5, false
            );

            CuestionarioResponse response = cuestionarioService.actualizar(1L, request);

            assertThat(response.nombre()).isEqualTo("Updated");
            assertThat(response.descripcion()).isEqualTo("Nueva desc");
            assertThat(response.tiempoLimite()).isEqualTo(90);
            assertThat(response.maxIntentos()).isEqualTo(5);
            assertThat(response.activo()).isFalse();
        }

        @Test
        @DisplayName("lanza EntityNotFoundException si cuestionario no existe")
        void actualizarNoExiste() {
            when(cuestionarioRepository.findById(99L)).thenReturn(Optional.empty());

            UpdateCuestionarioRequest request = new UpdateCuestionarioRequest(
                "X", null, null, null, null
            );

            assertThatThrownBy(() -> cuestionarioService.actualizar(99L, request))
                .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Eliminar cuestionario")
    class EliminarTests {

        @Test
        @DisplayName("elimina cuestionario existente")
        void eliminarExitoso() {
            when(cuestionarioRepository.existsById(1L)).thenReturn(true);

            cuestionarioService.eliminar(1L);

            verify(cuestionarioRepository).deleteById(1L);
        }

        @Test
        @DisplayName("lanza EntityNotFoundException si no existe")
        void eliminarNoExiste() {
            when(cuestionarioRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> cuestionarioService.eliminar(99L))
                .isInstanceOf(EntityNotFoundException.class);

            verify(cuestionarioRepository, never()).deleteById(any());
        }
    }
}
