package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.CalificarResultadoRequest;
import com.bitcriollo.plataforma.model.*;
import com.bitcriollo.plataforma.model.enums.EstadoResultado;
import com.bitcriollo.plataforma.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultadoServiceTest {
    @Mock private ResultadoRepository resultados;
    @Mock private EvaluacionRepository evaluaciones;
    @Mock private CursoRepository cursos;
    @Mock private CursoAccesoService acceso;
    @InjectMocks private ResultadoService service;
    private Resultado resultado;
    private Tarea tarea;
    private Curso curso;
    private Docente docente;

    @BeforeEach
    void preparar() {
        docente = new Docente();
        docente.setId(1L);
        curso = new Curso();
        curso.setId(10L);
        tarea = new Tarea();
        tarea.setId(20L);
        tarea.setCurso(curso);
        tarea.setFechaLimite(LocalDateTime.now().minusDays(1));
        Estudiante estudiante = new Estudiante();
        estudiante.setId(2L);
        resultado = new Resultado();
        resultado.setId(30L);
        resultado.setEvaluacion(tarea);
        resultado.setEstudiante(estudiante);
        resultado.setFechaRegistro(LocalDateTime.now());
        when(resultados.findById(30L)).thenReturn(Optional.of(resultado));
    }

    static Stream<Double> notasInvalidas() {
        return Stream.of(null, -0.1, 5.01, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @ParameterizedTest
    @MethodSource("notasInvalidas")
    void rechazaNotasFueraDeEscalaONoFinitas(Double nota) {
        when(acceso.esDocenteDelCurso(curso, docente)).thenReturn(true);
        CalificarResultadoRequest request = new CalificarResultadoRequest();
        request.setCalificacion(nota);
        assertThatThrownBy(() -> service.calificar(30L, request, docente)).isInstanceOf(IllegalArgumentException.class);
        assertThat(resultado.getEstado()).isEqualTo(EstadoResultado.PENDIENTE);
        verify(resultados, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 5.0})
    void aceptaLosLimitesDeLaEscala(double nota) {
        when(acceso.esDocenteDelCurso(curso, docente)).thenReturn(true);
        CalificarResultadoRequest request = new CalificarResultadoRequest();
        request.setCalificacion(nota);
        assertThat(service.calificar(30L, request, docente).getCalificacion()).isEqualTo(nota);
    }

    @Test
    void conservaLaPenalizacionPorEntregaTardia() {
        when(acceso.esDocenteDelCurso(curso, docente)).thenReturn(true);
        tarea.setPenalizacionTardanzaPorcentaje(20.0);
        CalificarResultadoRequest request = new CalificarResultadoRequest();
        request.setCalificacion(5.0);
        assertThat(service.calificar(30L, request, docente).getCalificacion()).isEqualTo(4.0);
    }

    @Test
    void otroDocenteNoPuedeCalificar() {
        CalificarResultadoRequest request = new CalificarResultadoRequest();
        request.setCalificacion(4.0);
        assertThatThrownBy(() -> service.calificar(30L, request, docente)).isInstanceOf(AccessDeniedException.class);
        verify(resultados, never()).save(any());
    }
}
