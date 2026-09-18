package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.EvaluacionResponse;
import com.bitcriollo.plataforma.model.CoordinadorAcademico;
import com.bitcriollo.plataforma.model.Curso;
import com.bitcriollo.plataforma.model.Estudiante;
import com.bitcriollo.plataforma.model.Evaluacion;
import com.bitcriollo.plataforma.model.Examen;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.model.enums.EstadoEvaluacion;
import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import com.bitcriollo.plataforma.repository.CursoRepository;
import com.bitcriollo.plataforma.repository.EvaluacionRepository;
import com.bitcriollo.plataforma.repository.InscripcionRepository;
import org.hibernate.Hibernate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;
    private final CursoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;

    public EvaluacionService(EvaluacionRepository evaluacionRepository,
            CursoRepository cursoRepository,
            InscripcionRepository inscripcionRepository) {
        this.evaluacionRepository = evaluacionRepository;
        this.cursoRepository = cursoRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Transactional(readOnly = true)
    public List<EvaluacionResponse> listarPorCurso(Long cursoId, Usuario usuario) {
        Curso curso = buscarCurso(cursoId);
        boolean puedeGestionar = puedeGestionar(curso, usuario);
        if (!puedeGestionar) {
            validarEstudianteInscrito(curso, usuario);
        }

        // Un estudiante solo ve lo que ya fue publicado
        return evaluacionRepository.findByCursoId(cursoId).stream()
                .filter(e -> puedeGestionar || e.getEstado() != EstadoEvaluacion.BORRADOR)
                .map(this::mapearResumen)
                .toList();
    }

    private Curso buscarCurso(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe un curso con id " + cursoId));
    }

    private boolean esDocenteDelCurso(Curso curso, Usuario usuario) {
        return curso.getDocente().getId().equals(usuario.getId());
    }

    private boolean puedeGestionar(Curso curso, Usuario usuario) {
        return usuario instanceof CoordinadorAcademico || esDocenteDelCurso(curso, usuario);
    }

    private void validarEstudianteInscrito(Curso curso, Usuario usuario) {
        boolean inscrito = usuario instanceof Estudiante
                && inscripcionRepository.findByEstudianteIdAndCursoId(usuario.getId(), curso.getId())
                        .filter(i -> i.getEstado() != EstadoInscripcion.CANCELADA)
                        .isPresent();
        if (!inscrito) {
            throw new AccessDeniedException("Debes estar inscrito en el curso para ver sus evaluaciones");
        }
    }

    private EvaluacionResponse mapearResumen(Evaluacion e) {
        String tipo = Hibernate.unproxy(e) instanceof Examen ? "EXAMEN" : "TAREA";
        return new EvaluacionResponse(
                e.getId(),
                e.getCurso().getId(),
                tipo,
                e.getTitulo(),
                e.getDescripcion(),
                e.getPesoPorcentual(),
                e.getEstado(),
                e.getFechaPublicacion(),
                e.getCreatedAt());
    }
}
