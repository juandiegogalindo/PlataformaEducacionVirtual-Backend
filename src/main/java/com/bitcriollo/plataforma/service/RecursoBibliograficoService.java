package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.RecursoBibliograficoRequest;
import com.bitcriollo.plataforma.dto.RecursoBibliograficoResponse;
import com.bitcriollo.plataforma.model.*;
import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import com.bitcriollo.plataforma.repository.CursoRepository;
import com.bitcriollo.plataforma.repository.InscripcionRepository;
import com.bitcriollo.plataforma.repository.RecursoBibliograficoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecursoBibliograficoService {

    private final RecursoBibliograficoRepository recursoRepository;
    private final CursoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;

    public RecursoBibliograficoService(RecursoBibliograficoRepository recursoRepository,
            CursoRepository cursoRepository,
            InscripcionRepository inscripcionRepository) {
        this.recursoRepository = recursoRepository;
        this.cursoRepository = cursoRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Transactional
    public RecursoBibliograficoResponse crearRecurso(Long cursoId, RecursoBibliograficoRequest request,
            Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(cursoId);
        validarEsDocenteDueno(curso, solicitante);

        RecursoBibliografico recurso = new RecursoBibliografico();
        recurso.setCurso(curso);
        recurso.setTitulo(request.getTitulo());
        recurso.setAutor(request.getAutor());
        recurso.setAnioPublicacion(request.getAnioPublicacion());
        recurso.setTipoRecurso(request.getTipoRecurso());
        recurso.setEnlace(request.getEnlace());

        recursoRepository.save(recurso);
        return mapearAResponse(recurso);
    }

    @Transactional(readOnly = true)
    public List<RecursoBibliograficoResponse> listarRecursos(Long cursoId, Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(cursoId);
        validarAccesoLectura(curso, solicitante);

        return recursoRepository.findByCursoId(cursoId).stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional
    public RecursoBibliograficoResponse actualizarRecurso(Long cursoId, Long recursoId,
            RecursoBibliograficoRequest request, Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(cursoId);
        validarEsDocenteDueno(curso, solicitante);
        RecursoBibliografico recurso = buscarRecursoDelCursoOLanzar(cursoId, recursoId);

        recurso.setTitulo(request.getTitulo());
        recurso.setAutor(request.getAutor());
        recurso.setAnioPublicacion(request.getAnioPublicacion());
        recurso.setTipoRecurso(request.getTipoRecurso());
        recurso.setEnlace(request.getEnlace());

        recursoRepository.save(recurso);
        return mapearAResponse(recurso);
    }

    @Transactional
    public void eliminarRecurso(Long cursoId, Long recursoId, Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(cursoId);
        validarEsDocenteDueno(curso, solicitante);
        RecursoBibliografico recurso = buscarRecursoDelCursoOLanzar(cursoId, recursoId);
        recursoRepository.delete(recurso);
    }

    private Curso buscarCursoOLanzar(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe un curso con id " + cursoId));
    }

    private RecursoBibliografico buscarRecursoDelCursoOLanzar(Long cursoId, Long recursoId) {
        RecursoBibliografico recurso = recursoRepository.findById(recursoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe un recurso con id " + recursoId));
        if (!recurso.getCurso().getId().equals(cursoId)) {
            throw new IllegalArgumentException("Ese recurso no pertenece a este curso");
        }
        return recurso;
    }

    private void validarEsDocenteDueno(Curso curso, Usuario solicitante) {
        boolean esDocenteDelCurso = curso.getDocente().getId().equals(solicitante.getId());
        if (!esDocenteDelCurso) {
            throw new AccessDeniedException("Solo el docente que dicta este curso puede gestionar sus recursos");
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

    private RecursoBibliograficoResponse mapearAResponse(RecursoBibliografico recurso) {
        return new RecursoBibliograficoResponse(
                recurso.getId(),
                recurso.getCurso().getId(),
                recurso.getTitulo(),
                recurso.getAutor(),
                recurso.getAnioPublicacion(),
                recurso.getTipoRecurso(),
                recurso.getEnlace(),
                recurso.getCreatedAt());
    }
}