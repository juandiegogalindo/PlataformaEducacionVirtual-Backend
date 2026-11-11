package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.model.Leccion;
import com.bitcriollo.plataforma.model.ProgresoLeccion;
import com.bitcriollo.plataforma.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgresoLeccionServiceTest {
    @Mock private ProgresoLeccionRepository progresos;
    @Mock private LeccionRepository lecciones;
    @Mock private CursoRepository cursos;
    @Mock private CursoAccesoService acceso;
    @InjectMocks private ProgresoLeccionService service;

    @Test
    void calculaElPorcentajeConElProgresoDelCursoSolicitado() {
        Leccion primera = new Leccion();
        primera.setId(1L);
        primera.setOrden(1);
        Leccion segunda = new Leccion();
        segunda.setId(2L);
        segunda.setOrden(2);
        ProgresoLeccion progreso = new ProgresoLeccion();
        progreso.setLeccion(primera);
        progreso.setCompletado(true);
        when(lecciones.findByCursoIdOrderByOrdenAsc(10L)).thenReturn(List.of(primera, segunda));
        when(progresos.findByEstudianteIdAndLeccionCursoId(20L, 10L)).thenReturn(List.of(progreso));
        var respuesta = service.construirProgreso(10L, 20L);
        assertThat(respuesta.getLeccionesCompletadas()).isEqualTo(1);
        assertThat(respuesta.getPorcentajeAvance()).isEqualTo(50.0);
    }

    @Test
    void unCursoSinLeccionesTieneCeroPorCiento() {
        assertThat(service.construirProgreso(10L, 20L).getPorcentajeAvance()).isZero();
    }
}
