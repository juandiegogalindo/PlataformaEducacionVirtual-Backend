package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.InscripcionRequest;
import com.bitcriollo.plataforma.dto.InscripcionResponse;
import com.bitcriollo.plataforma.model.Curso;
import com.bitcriollo.plataforma.model.Estudiante;
import com.bitcriollo.plataforma.model.Inscripcion;
import com.bitcriollo.plataforma.model.enums.EstadoCurso;
import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import com.bitcriollo.plataforma.repository.CursoRepository;
import com.bitcriollo.plataforma.repository.InscripcionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final CursoRepository cursoRepository;

    public InscripcionService(InscripcionRepository inscripcionRepository, CursoRepository cursoRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.cursoRepository = cursoRepository;
    }

    @Transactional
    public InscripcionResponse inscribirse(InscripcionRequest request, Estudiante estudiante) {
        Curso curso = cursoRepository.findById(request.getCursoId())
                .orElseThrow(() -> new IllegalArgumentException("No existe un curso con id " + request.getCursoId()));

        // Solo los cursos activos pueden recibir nuevas inscripciones.
        if (curso.getEstado() != EstadoCurso.ACTIVO) {
            throw new IllegalStateException("Este curso no esta disponible para inscripciones");
        }

        boolean yaInscrito = inscripcionRepository
                .findByEstudianteIdAndCursoId(estudiante.getId(), curso.getId())
                .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
                .isPresent();
        if (yaInscrito) {
            throw new IllegalStateException("Ya estas inscrito en este curso");
        }

        if (curso.getCupoMaximo() != null) {
            // Se cuentan únicamente las inscripciones activas para controlar el cupo disponible.
            long inscritosActivos = curso.getInscripciones().stream()
                    .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
                    .count();
            if (inscritosActivos >= curso.getCupoMaximo()) {
                throw new IllegalStateException("Este curso ya alcanzo su cupo maximo");
            }
        }

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setEstudiante(estudiante);
        inscripcion.setCurso(curso);
        inscripcion.setEstado(EstadoInscripcion.ACTIVA);

        inscripcionRepository.save(inscripcion);
        return mapearAResponse(inscripcion);
    }

    @Transactional
    public void cancelarInscripcion(Long inscripcionId, Estudiante estudiante) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("No existe esa inscripcion"));

        if (!inscripcion.getEstudiante().getId().equals(estudiante.getId())) {
            throw new AccessDeniedException("No puedes cancelar la inscripcion de otro estudiante");
        }

        // Una inscripción que ya no está activa no puede cancelarse nuevamente.
        if (inscripcion.getEstado() != EstadoInscripcion.ACTIVA) {
            throw new IllegalStateException("Esta inscripcion ya no esta activa");
        }

        inscripcion.setEstado(EstadoInscripcion.CANCELADA);
        inscripcionRepository.save(inscripcion);
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> misCursos(Estudiante estudiante) {
        return inscripcionRepository.findByEstudianteId(estudiante.getId()).stream()
                .map(this::mapearAResponse)
                .toList();
    }

    private InscripcionResponse mapearAResponse(Inscripcion inscripcion) {
        Curso curso = inscripcion.getCurso();
        return new InscripcionResponse(
                inscripcion.getId(),
                curso.getId(),
                curso.getNombre(),
                curso.getDocente().getNombre() + " " + curso.getDocente().getApellido(),
                inscripcion.getEstado(),
                inscripcion.getFechaInscripcion(),
                inscripcion.getCalificacionFinal());
    }
}