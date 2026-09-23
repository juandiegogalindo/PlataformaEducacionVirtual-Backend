package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.MensajeForoRequest;
import com.bitcriollo.plataforma.dto.MensajeForoResponse;
import com.bitcriollo.plataforma.model.*;
import com.bitcriollo.plataforma.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MensajeForoService {

    private final MensajeForoRepository mensajeRepository;
    private final ForoRepository foroRepository;
    private final CursoRepository cursoRepository;
    private final CursoAccesoService cursoAccesoService;

    public MensajeForoService(MensajeForoRepository mensajeRepository,
            ForoRepository foroRepository,
            CursoRepository cursoRepository,
            CursoAccesoService cursoAccesoService) {
        this.mensajeRepository = mensajeRepository;
        this.foroRepository = foroRepository;
        this.cursoRepository = cursoRepository;
        this.cursoAccesoService = cursoAccesoService;
    }

    @Transactional
    public MensajeForoResponse publicarMensaje(Long cursoId, MensajeForoRequest request, Usuario autor) {
        Curso curso = buscarCursoOLanzar(cursoId);
        validarAccesoAlForo(curso, autor);
        Foro foro = foroRepository.findByCursoId(cursoId)
                .orElseThrow(() -> new IllegalStateException("Este curso todavia no tiene foro asociado"));

        MensajeForo mensajePadre = null;
        if (request.getMensajePadreId() != null) {
            mensajePadre = mensajeRepository.findById(request.getMensajePadreId())
                    .orElseThrow(() -> new IllegalArgumentException("No existe el mensaje al que intentas responder"));
            // Una respuesta solo puede pertenecer a un mensaje del mismo foro.
            if (!mensajePadre.getForo().getId().equals(foro.getId())) {
                throw new IllegalArgumentException("Ese mensaje no pertenece al foro de este curso");
            }
        }

        MensajeForo mensaje = new MensajeForo();
        mensaje.setForo(foro);
        mensaje.setUsuario(autor);
        mensaje.setMensajePadre(mensajePadre);
        mensaje.setContenido(request.getContenido());

        mensajeRepository.save(mensaje);
        return mapearConHijos(mensaje);
    }

    @Transactional(readOnly = true)
    public List<MensajeForoResponse> listarMensajes(Long cursoId, Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(cursoId);
        validarAccesoAlForo(curso, solicitante);
        Foro foro = foroRepository.findByCursoId(cursoId)
                .orElseThrow(() -> new IllegalStateException("Este curso todavia no tiene foro asociado"));

        List<MensajeForo> raices = mensajeRepository
                .findByForoIdAndMensajePadreIsNullOrderByFechaPublicacionAsc(foro.getId());

        return raices.stream().map(this::mapearConHijos).toList();
    }

    @Transactional
    public MensajeForoResponse editarMensaje(Long cursoId, Long mensajeId, MensajeForoRequest request,
            Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(cursoId);
        MensajeForo mensaje = buscarMensajeDelCursoOLanzar(curso, mensajeId);
        validarAutorOModerador(curso, mensaje, solicitante);

        mensaje.setContenido(request.getContenido());
        mensaje.setEditado(true);

        mensajeRepository.save(mensaje);
        return mapearConHijos(mensaje);
    }

    @Transactional
    public void eliminarMensaje(Long cursoId, Long mensajeId, Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(cursoId);
        MensajeForo mensaje = buscarMensajeDelCursoOLanzar(curso, mensajeId);
        validarAutorOModerador(curso, mensaje, solicitante);
        mensajeRepository.delete(mensaje);
    }

    private Curso buscarCursoOLanzar(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe un curso con id " + cursoId));
    }

    private MensajeForo buscarMensajeDelCursoOLanzar(Curso curso, Long mensajeId) {
        MensajeForo mensaje = mensajeRepository.findById(mensajeId)
                .orElseThrow(() -> new IllegalArgumentException("No existe un mensaje con id " + mensajeId));
        // Se valida que el mensaje pertenezca al curso antes de permitir su modificación o eliminación.
        if (!mensaje.getForo().getCurso().getId().equals(curso.getId())) {
            throw new IllegalArgumentException("Ese mensaje no pertenece a este curso");
        }
        return mensaje;
    }

    private void validarAccesoAlForo(Curso curso, Usuario solicitante) {
        // Solo el docente del curso y los estudiantes con inscripción activa pueden participar en el foro.
        if (cursoAccesoService.esDocenteDelCurso(curso, solicitante)
                || cursoAccesoService.estaInscritoActivo(curso, solicitante)) {
            return;
        }

        throw new AccessDeniedException("No tienes acceso al foro de este curso");
    }

    private void validarAutorOModerador(Curso curso, MensajeForo mensaje, Usuario solicitante) {
        boolean esAutor = mensaje.getUsuario().getId().equals(solicitante.getId());
        // Solo el autor o el docente responsable del curso pueden modificar o eliminar el mensaje.
        if (!esAutor && !cursoAccesoService.esDocenteDelCurso(curso, solicitante)) {
            throw new AccessDeniedException("Solo el autor o el docente del curso pueden modificar este mensaje");
        }
    }

    private MensajeForoResponse mapearConHijos(MensajeForo mensaje) {
        List<MensajeForoResponse> respuestas = mensajeRepository.findByMensajePadreId(mensaje.getId()).stream()
                .map(this::mapearConHijos)
                .toList();

        return new MensajeForoResponse(
                mensaje.getId(),
                mensaje.getUsuario().getNombre() + " " + mensaje.getUsuario().getApellido(),
                mensaje.getUsuario().getClass().getSimpleName(),
                mensaje.getContenido(),
                mensaje.isEditado(),
                mensaje.getFechaPublicacion(),
                respuestas);
    }
}