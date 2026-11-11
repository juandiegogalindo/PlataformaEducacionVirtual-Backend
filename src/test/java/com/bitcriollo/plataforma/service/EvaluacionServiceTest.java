package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.TareaRequest;
import com.bitcriollo.plataforma.model.*;
import com.bitcriollo.plataforma.model.enums.EstadoEvaluacion;
import com.bitcriollo.plataforma.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluacionServiceTest {
    @Mock private EvaluacionRepository evaluaciones;
    @Mock private ExamenRepository examenes;
    @Mock private TareaRepository tareas;
    @Mock private CursoRepository cursos;
    @Mock private CursoAccesoService acceso;
    @InjectMocks private EvaluacionService service;
    private Curso curso;
    private Docente docente;
    private Estudiante estudiante;
    private Tarea tarea;

    @BeforeEach
    void preparar() {
        docente = new Docente();
        docente.setId(1L);
        estudiante = new Estudiante();
        estudiante.setId(2L);
        curso = new Curso();
        curso.setId(10L);
        curso.setDocente(docente);
        tarea = new Tarea();
        tarea.setId(20L);
        tarea.setCurso(curso);
        tarea.setPesoPorcentual(70.0);
        tarea.setEstado(EstadoEvaluacion.PUBLICADA);
    }

    @Test
    void completarUnCursoConservaLaLecturaDeSusEvaluacionesPublicadas() {
        when(cursos.findById(10L)).thenReturn(Optional.of(curso));
        when(acceso.tieneAccesoDeLectura(curso, estudiante)).thenReturn(true);
        when(evaluaciones.findByCursoId(10L)).thenReturn(List.of(tarea));
        assertThat(service.listarPorCurso(10L, estudiante)).hasSize(1);
    }

    @Test
    void laLecturaDeUnaTareaTampocoExigeUnaInscripcionActiva() {
        when(tareas.findById(20L)).thenReturn(Optional.of(tarea));
        when(acceso.tieneAccesoDeLectura(curso, estudiante)).thenReturn(true);
        assertThat(service.obtenerTarea(20L, estudiante).getId()).isEqualTo(20L);
    }

    @Test
    void elEstudianteNoPuedeVerUnaTareaEnBorrador() {
        tarea.setEstado(EstadoEvaluacion.BORRADOR);
        when(tareas.findById(20L)).thenReturn(Optional.of(tarea));
        when(acceso.tieneAccesoDeLectura(curso, estudiante)).thenReturn(true);
        assertThatThrownBy(() -> service.obtenerTarea(20L, estudiante)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unaInscripcionCanceladaNoPermiteConsultarEvaluaciones() {
        when(cursos.findById(10L)).thenReturn(Optional.of(curso));
        assertThatThrownBy(() -> service.listarPorCurso(10L, estudiante)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void noPermiteSuperarElPesoTotalDelCurso() {
        when(cursos.findByIdConBloqueo(10L)).thenReturn(Optional.of(curso));
        when(acceso.esDocenteDelCurso(curso, docente)).thenReturn(true);
        when(evaluaciones.findByCursoId(10L)).thenReturn(List.of(tarea));
        TareaRequest request = nuevaTarea(40.0);
        assertThatThrownBy(() -> service.crearTarea(10L, request, docente)).isInstanceOf(IllegalStateException.class);
        verify(tareas, never()).save(any());
    }

    @Test
    void permiteCompletarExactamenteElCienPorCiento() {
        when(cursos.findByIdConBloqueo(10L)).thenReturn(Optional.of(curso));
        when(acceso.esDocenteDelCurso(curso, docente)).thenReturn(true);
        when(evaluaciones.findByCursoId(10L)).thenReturn(List.of(tarea));
        assertThat(service.crearTarea(10L, nuevaTarea(30.0), docente).getPesoPorcentual()).isEqualTo(30.0);
    }

    private TareaRequest nuevaTarea(double peso) {
        TareaRequest request = new TareaRequest();
        request.setTitulo("Tarea");
        request.setPesoPorcentual(peso);
        request.setFechaLimite(LocalDateTime.now().plusDays(1));
        return request;
    }
}
