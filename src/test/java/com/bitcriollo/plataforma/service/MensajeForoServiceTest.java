package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.MensajeForoRequest;
import com.bitcriollo.plataforma.model.*;
import com.bitcriollo.plataforma.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensajeForoServiceTest {
    @Mock private MensajeForoRepository mensajes;
    @Mock private ForoRepository foros;
    @Mock private CursoRepository cursos;
    @Mock private CursoAccesoService acceso;
    @InjectMocks private MensajeForoService service;
    private Curso curso;
    private Estudiante estudiante;
    private MensajeForo mensaje;
    private MensajeForoRequest request;

    @BeforeEach
    void preparar() {
        curso = new Curso();
        curso.setId(1L);
        estudiante = new Estudiante();
        estudiante.setId(2L);
        Foro foro = new Foro();
        foro.setCurso(curso);
        mensaje = new MensajeForo();
        mensaje.setId(3L);
        mensaje.setUsuario(estudiante);
        mensaje.setForo(foro);
        mensaje.setContenido("Original");
        request = new MensajeForoRequest();
        request.setContenido("Editado");
        when(cursos.findById(1L)).thenReturn(Optional.of(curso));
    }

    @Test
    void unAutorSinParticipacionVigenteNoPuedeEditar() {
        assertThatThrownBy(() -> service.editarMensaje(1L, 3L, request, estudiante))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(mensaje.getContenido()).isEqualTo("Original");
        verifyNoInteractions(mensajes);
    }

    @Test
    void unAutorSinParticipacionVigenteNoPuedeEliminar() {
        assertThatThrownBy(() -> service.eliminarMensaje(1L, 3L, estudiante))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(mensajes);
    }

    @Test
    void elAutorConInscripcionActivaPuedeEditar() {
        when(acceso.estaInscritoActivo(curso, estudiante)).thenReturn(true);
        when(mensajes.findById(3L)).thenReturn(Optional.of(mensaje));
        assertThat(service.editarMensaje(1L, 3L, request, estudiante).getContenido()).isEqualTo("Editado");
        assertThat(mensaje.isEditado()).isTrue();
    }

    @Test
    void elCoordinadorConservaSuPermisoDeModeracion() {
        CoordinadorAcademico coordinador = new CoordinadorAcademico();
        coordinador.setId(4L);
        when(acceso.esCoordinador(coordinador)).thenReturn(true);
        when(mensajes.findById(3L)).thenReturn(Optional.of(mensaje));
        service.eliminarMensaje(1L, 3L, coordinador);
        verify(mensajes).delete(mensaje);
    }
}
