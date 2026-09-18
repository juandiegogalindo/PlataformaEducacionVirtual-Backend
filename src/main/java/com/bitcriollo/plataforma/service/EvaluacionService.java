package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.EvaluacionResponse;
import com.bitcriollo.plataforma.dto.ExamenRequest;
import com.bitcriollo.plataforma.dto.ExamenResponse;
import com.bitcriollo.plataforma.dto.TareaRequest;
import com.bitcriollo.plataforma.dto.TareaResponse;
import com.bitcriollo.plataforma.model.CoordinadorAcademico;
import com.bitcriollo.plataforma.model.Curso;
import com.bitcriollo.plataforma.model.Estudiante;
import com.bitcriollo.plataforma.model.Evaluacion;
import com.bitcriollo.plataforma.model.Examen;
import com.bitcriollo.plataforma.model.Tarea;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.model.enums.EstadoEvaluacion;
import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import com.bitcriollo.plataforma.repository.CursoRepository;
import com.bitcriollo.plataforma.repository.EvaluacionRepository;
import com.bitcriollo.plataforma.repository.ExamenRepository;
import com.bitcriollo.plataforma.repository.InscripcionRepository;
import com.bitcriollo.plataforma.repository.TareaRepository;
import org.hibernate.Hibernate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluacionService {

    private static final double PESO_TOTAL_MAXIMO = 100.0;

    private final EvaluacionRepository evaluacionRepository;
    private final ExamenRepository examenRepository;
    private final TareaRepository tareaRepository;
    private final CursoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;

    public EvaluacionService(EvaluacionRepository evaluacionRepository,
            ExamenRepository examenRepository,
            TareaRepository tareaRepository,
            CursoRepository cursoRepository,
            InscripcionRepository inscripcionRepository) {
        this.evaluacionRepository = evaluacionRepository;
        this.examenRepository = examenRepository;
        this.tareaRepository = tareaRepository;
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

    @Transactional
    public ExamenResponse crearExamen(Long cursoId, ExamenRequest request, Usuario docente) {
        Curso curso = buscarCurso(cursoId);
        validarDocenteDelCurso(curso, docente);
        validarPesoDisponible(cursoId, request.getPesoPorcentual());

        Examen examen = new Examen();
        cargarDatosBase(examen, curso, request.getTitulo(), request.getDescripcion(), request.getPesoPorcentual());
        examen.setFechaAplicacion(request.getFechaAplicacion());
        examen.setTiempoLimite(request.getTiempoLimite());
        examen.setNumeroIntentosPermitidos(
                request.getNumeroIntentosPermitidos() != null ? request.getNumeroIntentosPermitidos() : 1);
        examen.setAleatorio(request.isAleatorio());

        examenRepository.save(examen);
        return mapearExamen(examen);
    }

    @Transactional(readOnly = true)
    public ExamenResponse obtenerExamen(Long id, Usuario usuario) {
        Examen examen = examenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un examen con id " + id));
        validarAccesoLectura(examen, usuario);
        return mapearExamen(examen);
    }

    @Transactional
    public TareaResponse crearTarea(Long cursoId, TareaRequest request, Usuario docente) {
        Curso curso = buscarCurso(cursoId);
        validarDocenteDelCurso(curso, docente);
        validarPesoDisponible(cursoId, request.getPesoPorcentual());

        Tarea tarea = new Tarea();
        cargarDatosBase(tarea, curso, request.getTitulo(), request.getDescripcion(), request.getPesoPorcentual());
        tarea.setInstrucciones(request.getInstrucciones());
        tarea.setFechaLimite(request.getFechaLimite());
        tarea.setPermiteEntregaTardia(request.isPermiteEntregaTardia());
        if (request.isPermiteEntregaTardia()) {
            Double penalizacion = request.getPenalizacionTardanzaPorcentaje();
            tarea.setPenalizacionTardanzaPorcentaje(penalizacion != null ? penalizacion : 0.0);
        }

        tareaRepository.save(tarea);
        return mapearTarea(tarea);
    }

    @Transactional(readOnly = true)
    public TareaResponse obtenerTarea(Long id, Usuario usuario) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe una tarea con id " + id));
        validarAccesoLectura(tarea, usuario);
        return mapearTarea(tarea);
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

    private void validarDocenteDelCurso(Curso curso, Usuario usuario) {
        if (!esDocenteDelCurso(curso, usuario)) {
            throw new AccessDeniedException("Solo el docente del curso puede gestionar sus evaluaciones");
        }
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

    private void validarAccesoLectura(Evaluacion evaluacion, Usuario usuario) {
        Curso curso = evaluacion.getCurso();
        if (puedeGestionar(curso, usuario)) {
            return;
        }
        validarEstudianteInscrito(curso, usuario);
        if (evaluacion.getEstado() == EstadoEvaluacion.BORRADOR) {
            // No se revela la existencia de evaluaciones sin publicar
            throw new IllegalArgumentException("No existe una evaluacion con id " + evaluacion.getId());
        }
    }

    private void validarPesoDisponible(Long cursoId, Double pesoNuevo) {
        double pesoActual = evaluacionRepository.findByCursoId(cursoId).stream()
                .mapToDouble(Evaluacion::getPesoPorcentual)
                .sum();
        if (pesoActual + pesoNuevo > PESO_TOTAL_MAXIMO + 1e-9) {
            throw new IllegalStateException("La suma de los pesos de las evaluaciones del curso no puede superar "
                    + PESO_TOTAL_MAXIMO + "% (actual: " + pesoActual + "%)");
        }
    }

    private void cargarDatosBase(Evaluacion evaluacion, Curso curso, String titulo, String descripcion, Double peso) {
        evaluacion.setCurso(curso);
        evaluacion.setTitulo(titulo);
        evaluacion.setDescripcion(descripcion);
        evaluacion.setPesoPorcentual(peso);
        evaluacion.setEstado(EstadoEvaluacion.BORRADOR);
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

    private ExamenResponse mapearExamen(Examen e) {
        return new ExamenResponse(
                e.getId(),
                e.getCurso().getId(),
                e.getTitulo(),
                e.getDescripcion(),
                e.getPesoPorcentual(),
                e.getEstado(),
                e.getFechaPublicacion(),
                e.getFechaAplicacion(),
                e.getTiempoLimite(),
                e.getNumeroIntentosPermitidos(),
                e.isAleatorio(),
                e.getCreatedAt());
    }

    private TareaResponse mapearTarea(Tarea t) {
        return new TareaResponse(
                t.getId(),
                t.getCurso().getId(),
                t.getTitulo(),
                t.getDescripcion(),
                t.getPesoPorcentual(),
                t.getEstado(),
                t.getFechaPublicacion(),
                t.getInstrucciones(),
                t.getFechaLimite(),
                t.isPermiteEntregaTardia(),
                t.getPenalizacionTardanzaPorcentaje(),
                t.getCreatedAt());
    }
}
