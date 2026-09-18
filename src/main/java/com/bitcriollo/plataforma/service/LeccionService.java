package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.LeccionRequest;
import com.bitcriollo.plataforma.dto.LeccionResponse;
import com.bitcriollo.plataforma.model.*;
import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import com.bitcriollo.plataforma.repository.CursoRepository;
import com.bitcriollo.plataforma.repository.InscripcionRepository;
import com.bitcriollo.plataforma.repository.LeccionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LeccionService {

    private final LeccionRepository leccionRepository;
    private final CursoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;

    public LeccionService(LeccionRepository leccionRepository,
            CursoRepository cursoRepository,
            InscripcionRepository inscripcionRepository) {
        this.leccionRepository = leccionRepository;
        this.cursoRepository = cursoRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Transactional
    public LeccionResponse crearLeccion(Long cursoId, LeccionRequest request, Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(cursoId);
        validarEsDocenteDueno(curso, solicitante);

        Leccion leccion = new Leccion();
        leccion.setCurso(curso);
        leccion.setTitulo(request.getTitulo());
        leccion.setContenido(request.getContenido());
        leccion.setTipoContenido(request.getTipoContenido());
        leccion.setDuracionMinutos(request.getDuracionMinutos());
        leccion.setOrden(request.getOrden());

        leccionRepository.save(leccion);
        return mapearAResponse(leccion);
    }

    @Transactional(readOnly = true)
    public List<LeccionResponse> listarLecciones(Long cursoId, Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(cursoId);
        validarAccesoLectura(curso, solicitante);

        return leccionRepository.findByCursoIdOrderByOrdenAsc(cursoId).stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional
    public LeccionResponse actualizarLeccion(Long cursoId, Long leccionId, LeccionRequest request,
            Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(cursoId);
        validarEsDocenteDueno(curso, solicitante);
        Leccion leccion = buscarLeccionDelCursoOLanzar(cursoId, leccionId);

        leccion.setTitulo(request.getTitulo());
        leccion.setContenido(request.getContenido());
        leccion.setTipoContenido(request.getTipoContenido());
        leccion.setDuracionMinutos(request.getDuracionMinutos());
        leccion.setOrden(request.getOrden());

        leccionRepository.save(leccion);
        return mapearAResponse(leccion);
    }

    @Transactional
    public void eliminarLeccion(Long cursoId, Long leccionId, Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(cursoId);
        validarEsDocenteDueno(curso, solicitante);
        Leccion leccion = buscarLeccionDelCursoOLanzar(cursoId, leccionId);
        leccionRepository.delete(leccion);
    }

    private Curso buscarCursoOLanzar(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe un curso con id " + cursoId));
    }

    private Leccion buscarLeccionDelCursoOLanzar(Long cursoId, Long leccionId) {
        Leccion leccion = leccionRepository.findById(leccionId)
                .orElseThrow(() -> new IllegalArgumentException("No existe una leccion con id " + leccionId));
        if (!leccion.getCurso().getId().equals(cursoId)) {
            throw new IllegalArgumentException("Esa leccion no pertenece a este curso");
        }
        return leccion;
    }

    private void validarEsDocenteDueno(Curso curso, Usuario solicitante) {
        boolean esDocenteDelCurso = curso.getDocente().getId().equals(solicitante.getId());
        if (!esDocenteDelCurso) {
            throw new AccessDeniedException("Solo el docente que dicta este curso puede gestionar sus lecciones");
        }
    }

    private void validarAccesoLectura(Curso curso, Usuario solicitante) {
        boolean esDocenteDelCurso = curso.getDocente().getId().equals(solicitante.getId());
        if (esDocenteDelCurso) {
            return;
        }

        if (solicitante instanceof Estudiante) {
            boolean inscritoActivo = inscripcionRepository
                    .findByEstudianteIdAndCursoId(solicitante.getId(), curso.getId())
                    .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
                    .isPresent();
            if (inscritoActivo) {
                return;
            }
        }

        throw new AccessDeniedException("No tienes acceso al contenido de este curso");
    }

    private LeccionResponse mapearAResponse(Leccion leccion) {
        return new LeccionResponse(
                leccion.getId(),
                leccion.getCurso().getId(),
                leccion.getTitulo(),
                leccion.getContenido(),
                leccion.getTipoContenido(),
                leccion.getDuracionMinutos(),
                leccion.getOrden(),
                leccion.getCreatedAt());
    }
}