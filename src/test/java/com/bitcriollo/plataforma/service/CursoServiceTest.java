package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.CursoRequest;
import com.bitcriollo.plataforma.dto.HorarioRequest;
import com.bitcriollo.plataforma.model.Curso;
import com.bitcriollo.plataforma.model.Docente;
import com.bitcriollo.plataforma.model.enums.DiaSemana;
import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import com.bitcriollo.plataforma.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {
    @Mock private CursoRepository cursos;
    @Mock private DocenteRepository docentes;
    @Mock private ForoRepository foros;
    @Mock private CursoAccesoService acceso;
    @Mock private InscripcionRepository inscripciones;
    @InjectMocks private CursoService service;
    private CursoRequest request;
    private Docente docente;
    private Curso curso;

    @BeforeEach
    void preparar() {
        docente = new Docente();
        docente.setId(1L);
        docente.setNombre("Ana");
        docente.setApellido("Docente");
        curso = new Curso();
        curso.setId(10L);
        curso.setDocente(docente);
        curso.setCreadoPor(docente);
        request = new CursoRequest();
        request.setNombre("Curso");
        request.setFechaInicio(LocalDate.of(2026, 11, 10));
        request.setFechaFin(LocalDate.of(2026, 12, 10));
    }

    private void prepararEdicion() {
        when(cursos.findByIdConBloqueo(10L)).thenReturn(Optional.of(curso));
        when(acceso.esDocenteDelCurso(curso, docente)).thenReturn(true);
    }

    @Test
    void rechazaCrearUnCursoConFechasInvertidas() {
        request.setFechaFin(request.getFechaInicio().minusDays(1));
        assertThatThrownBy(() -> service.crearCurso(request, docente)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(cursos, foros);
    }

    @Test
    void rechazaEditarUnCursoConFechasInvertidas() {
        request.setFechaFin(request.getFechaInicio().minusDays(1));
        when(cursos.findByIdConBloqueo(10L)).thenReturn(Optional.of(curso));
        assertThatThrownBy(() -> service.actualizarCurso(10L, request, docente)).isInstanceOf(IllegalArgumentException.class);
        verify(cursos, never()).save(any());
    }

    @Test
    void permiteUnCursoDeUnSoloDia() {
        prepararEdicion();
        request.setFechaFin(request.getFechaInicio());
        assertThatCode(() -> service.actualizarCurso(10L, request, docente)).doesNotThrowAnyException();
        assertThat(curso.getFechaFin()).isEqualTo(request.getFechaInicio());
    }

    @Test
    void rechazaUnaSesionNulaConErrorDeSolicitud() {
        request.setHorarios(Arrays.asList((HorarioRequest) null));
        assertThatThrownBy(() -> service.crearCurso(request, docente))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Cada sesion");
    }

    @Test
    void rechazaUnaSesionIncompleta() {
        request.setHorarios(List.of(new HorarioRequest()));
        assertThatThrownBy(() -> service.crearCurso(request, docente)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaHorasInvertidas() {
        HorarioRequest horario = new HorarioRequest();
        horario.setDiaSemana(DiaSemana.LUNES);
        horario.setHoraInicio(LocalTime.of(12, 0));
        horario.setHoraFin(LocalTime.of(11, 0));
        request.setHorarios(List.of(horario));
        assertThatThrownBy(() -> service.crearCurso(request, docente)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void noPermiteReducirElCupoPorDebajoDeLosInscritos() {
        prepararEdicion();
        request.setCupoMaximo(1);
        when(inscripciones.countByCursoIdAndEstado(10L, EstadoInscripcion.ACTIVA)).thenReturn(2L);
        assertThatThrownBy(() -> service.actualizarCurso(10L, request, docente)).isInstanceOf(IllegalStateException.class);
        verify(cursos, never()).save(any());
    }

    @Test
    void permiteUnCupoIgualALosInscritos() {
        prepararEdicion();
        request.setCupoMaximo(2);
        when(inscripciones.countByCursoIdAndEstado(10L, EstadoInscripcion.ACTIVA)).thenReturn(2L);
        assertThat(service.actualizarCurso(10L, request, docente).getCupoMaximo()).isEqualTo(2);
    }
}
