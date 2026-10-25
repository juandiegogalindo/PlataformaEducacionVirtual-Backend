package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.CursoProgresoResponse;
import com.bitcriollo.plataforma.dto.EstudianteInscritoResponse;
import com.bitcriollo.plataforma.model.Administrador;
import com.bitcriollo.plataforma.model.Curso;
import com.bitcriollo.plataforma.model.Estudiante;
import com.bitcriollo.plataforma.model.Inscripcion;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import com.bitcriollo.plataforma.repository.CursoRepository;
import com.bitcriollo.plataforma.repository.InscripcionRepository;
import com.bitcriollo.plataforma.repository.LeccionRepository;
import com.bitcriollo.plataforma.repository.ProgresoLeccionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SeguimientoCursoService {

    private final CursoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final LeccionRepository leccionRepository;
    private final ProgresoLeccionRepository progresoRepository;
    private final ProgresoLeccionService progresoLeccionService;
    private final CursoAccesoService cursoAccesoService;

    public SeguimientoCursoService(CursoRepository cursoRepository,
            InscripcionRepository inscripcionRepository,
            LeccionRepository leccionRepository,
            ProgresoLeccionRepository progresoRepository,
            ProgresoLeccionService progresoLeccionService,
            CursoAccesoService cursoAccesoService) {
        this.cursoRepository = cursoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.leccionRepository = leccionRepository;
        this.progresoRepository = progresoRepository;
        this.progresoLeccionService = progresoLeccionService;
        this.cursoAccesoService = cursoAccesoService;
    }

    @Transactional(readOnly = true)
    public List<EstudianteInscritoResponse> listarInscritos(Long cursoId, EstadoInscripcion estado,
            Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(cursoId);
        validarPuedeVerSeguimiento(curso, solicitante);

        int totalLecciones = leccionRepository.findByCursoIdOrderByOrdenAsc(cursoId).size();

        // Una sola consulta con las lecciones completadas de todos los estudiantes del curso.
        Map<Long, Long> completadasPorEstudiante = progresoRepository.contarCompletadasPorEstudiante(cursoId)
                .stream()
                .collect(Collectors.toMap(
                        fila -> ((Number) fila[0]).longValue(),
                        fila -> ((Number) fila[1]).longValue()));

        return inscripcionRepository.findByCursoId(cursoId).stream()
                .filter(i -> estado == null || i.getEstado() == estado)
                .sorted(Comparator
                        .comparing((Inscripcion i) -> i.getEstudiante().getApellido())
                        .thenComparing(i -> i.getEstudiante().getNombre()))
                .map(i -> mapearAResponse(i, totalLecciones,
                        completadasPorEstudiante.getOrDefault(i.getEstudiante().getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public CursoProgresoResponse obtenerProgresoDeEstudiante(Long cursoId, Long estudianteId,
            Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(cursoId);
        validarPuedeVerSeguimiento(curso, solicitante);

        // Solo se consulta el avance de estudiantes que tienen (o tuvieron) una inscripcion en este curso.
        if (inscripcionRepository.findByEstudianteIdAndCursoId(estudianteId, cursoId).isEmpty()) {
            throw new IllegalArgumentException("Ese estudiante no esta inscrito en este curso");
        }

        return progresoLeccionService.construirProgreso(cursoId, estudianteId);
    }

    private Curso buscarCursoOLanzar(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe un curso con id " + cursoId));
    }

    private void validarPuedeVerSeguimiento(Curso curso, Usuario solicitante) {
        // Ven el seguimiento el docente del curso, los coordinadores y los administradores.
        boolean esDocenteDelCurso = cursoAccesoService.esDocenteDelCurso(curso, solicitante);
        boolean esGestion = cursoAccesoService.esCoordinador(solicitante) || solicitante instanceof Administrador;
        if (!esDocenteDelCurso && !esGestion) {
            throw new AccessDeniedException(
                    "Solo el docente del curso, un coordinador o un administrador pueden ver a sus estudiantes");
        }
    }

    private EstudianteInscritoResponse mapearAResponse(Inscripcion inscripcion, int totalLecciones,
            long completadas) {
        Estudiante estudiante = inscripcion.getEstudiante();
        double porcentaje = totalLecciones == 0 ? 0.0 : (completadas * 100.0) / totalLecciones;
        return new EstudianteInscritoResponse(
                inscripcion.getId(),
                estudiante.getId(),
                estudiante.getNombre() + " " + estudiante.getApellido(),
                estudiante.getCorreo(),
                estudiante.getCodigoEstudiantil(),
                estudiante.getProgramaAcademico(),
                estudiante.getSemestre(),
                inscripcion.getEstado(),
                inscripcion.getFechaInscripcion(),
                inscripcion.getCalificacionFinal(),
                totalLecciones,
                (int) completadas,
                porcentaje);
    }
}