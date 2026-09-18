package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.CalificarResultadoRequest;
import com.bitcriollo.plataforma.dto.RegistrarResultadoRequest;
import com.bitcriollo.plataforma.dto.ResultadoResponse;
import com.bitcriollo.plataforma.model.Curso;
import com.bitcriollo.plataforma.model.Estudiante;
import com.bitcriollo.plataforma.model.Evaluacion;
import com.bitcriollo.plataforma.model.Examen;
import com.bitcriollo.plataforma.model.Resultado;
import com.bitcriollo.plataforma.model.Tarea;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.model.enums.EstadoEvaluacion;
import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import com.bitcriollo.plataforma.model.enums.EstadoResultado;
import com.bitcriollo.plataforma.repository.CursoRepository;
import com.bitcriollo.plataforma.repository.EvaluacionRepository;
import com.bitcriollo.plataforma.repository.InscripcionRepository;
import com.bitcriollo.plataforma.repository.ResultadoRepository;
import org.hibernate.Hibernate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResultadoService {

    // Escala de calificacion: de 0.0 a NOTA_MAXIMA
    private static final double NOTA_MAXIMA = 5.0;

    private final ResultadoRepository resultadoRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final InscripcionRepository inscripcionRepository;
    private final CursoRepository cursoRepository;

    public ResultadoService(ResultadoRepository resultadoRepository,
            EvaluacionRepository evaluacionRepository,
            InscripcionRepository inscripcionRepository,
            CursoRepository cursoRepository) {
        this.resultadoRepository = resultadoRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.cursoRepository = cursoRepository;
    }

    @Transactional
    public ResultadoResponse registrarResultado(Long evaluacionId, RegistrarResultadoRequest request, Usuario usuario) {
        if (!(usuario instanceof Estudiante estudiante)) {
            throw new AccessDeniedException("Solo un estudiante puede registrar intentos o entregas");
        }

        Evaluacion evaluacion = (Evaluacion) Hibernate.unproxy(evaluacionRepository.findById(evaluacionId)
                .orElseThrow(() -> new IllegalArgumentException("No existe una evaluacion con id " + evaluacionId)));

        boolean inscripcionActiva = inscripcionRepository
                .findByEstudianteIdAndCursoId(estudiante.getId(), evaluacion.getCurso().getId())
                .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
                .isPresent();
        if (!inscripcionActiva) {
            throw new AccessDeniedException("Debes tener una inscripcion activa en el curso");
        }

        if (evaluacion.getEstado() == EstadoEvaluacion.BORRADOR) {
            throw new IllegalArgumentException("No existe una evaluacion con id " + evaluacionId);
        }
        if (evaluacion.getEstado() == EstadoEvaluacion.CERRADA) {
            throw new IllegalStateException("La evaluacion esta cerrada");
        }

        List<Resultado> previos = resultadoRepository.findByEvaluacionIdAndEstudianteId(evaluacionId,
                estudiante.getId());
        LocalDateTime ahora = LocalDateTime.now();

        if (evaluacion instanceof Examen examen) {
            if (ahora.isBefore(examen.getFechaAplicacion())) {
                throw new IllegalStateException("El examen aun no esta disponible");
            }
            if (previos.size() >= examen.getNumeroIntentosPermitidos()) {
                throw new IllegalStateException("Ya usaste todos los intentos permitidos para este examen");
            }
        } else if (evaluacion instanceof Tarea tarea) {
            if (!previos.isEmpty()) {
                throw new IllegalStateException("Ya registraste la entrega de esta tarea");
            }
            if (!StringUtils.hasText(request.getContenido())) {
                throw new IllegalArgumentException("La entrega de una tarea requiere contenido");
            }
            if (ahora.isAfter(tarea.getFechaLimite()) && !tarea.isPermiteEntregaTardia()) {
                throw new IllegalStateException("La fecha limite de entrega ya paso");
            }
        }

        Resultado resultado = new Resultado();
        resultado.setEvaluacion(evaluacion);
        resultado.setEstudiante(estudiante);
        resultado.setNumeroIntento(previos.size() + 1);
        resultado.setContenidoEntrega(request.getContenido());

        resultadoRepository.save(resultado);
        return mapearAResponse(resultado);
    }

    @Transactional
    public ResultadoResponse calificar(Long resultadoId, CalificarResultadoRequest request, Usuario docente) {
        Resultado resultado = resultadoRepository.findById(resultadoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe un resultado con id " + resultadoId));
        validarDocenteDelCurso(resultado.getEvaluacion().getCurso(), docente);

        if (request.getCalificacion() > NOTA_MAXIMA) {
            throw new IllegalArgumentException("La calificacion no puede superar " + NOTA_MAXIMA);
        }

        double nota = request.getCalificacion();
        Evaluacion evaluacion = (Evaluacion) Hibernate.unproxy(resultado.getEvaluacion());
        if (evaluacion instanceof Tarea tarea && esEntregaTardia(resultado, tarea)
                && tarea.getPenalizacionTardanzaPorcentaje() != null) {
            nota = nota * (1 - tarea.getPenalizacionTardanzaPorcentaje() / 100.0);
        }

        resultado.setCalificacion(Math.round(nota * 100.0) / 100.0);
        resultado.setRetroalimentacion(request.getRetroalimentacion());
        resultado.setEstado(EstadoResultado.CALIFICADO);

        resultadoRepository.save(resultado);
        return mapearAResponse(resultado);
    }

    @Transactional(readOnly = true)
    public List<ResultadoResponse> misCalificaciones(Usuario estudiante) {
        return resultadoRepository.findByEstudianteId(estudiante.getId()).stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResultadoResponse> calificacionesDelCurso(Long cursoId, Usuario docente) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe un curso con id " + cursoId));
        validarDocenteDelCurso(curso, docente);

        return resultadoRepository.findByEvaluacionCursoId(cursoId).stream()
                .map(this::mapearAResponse)
                .toList();
    }

    private void validarDocenteDelCurso(Curso curso, Usuario usuario) {
        if (!curso.getDocente().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("Solo el docente del curso puede gestionar sus calificaciones");
        }
    }

    private boolean esEntregaTardia(Resultado resultado, Tarea tarea) {
        return resultado.getFechaRegistro() != null && resultado.getFechaRegistro().isAfter(tarea.getFechaLimite());
    }

    private ResultadoResponse mapearAResponse(Resultado r) {
        // La evaluacion llega como proxy lazy: se desenvuelve para poder distinguir Tarea de Examen
        Evaluacion evaluacion = (Evaluacion) Hibernate.unproxy(r.getEvaluacion());
        boolean tardia = evaluacion instanceof Tarea tarea && esEntregaTardia(r, tarea);
        Estudiante estudiante = r.getEstudiante();

        return new ResultadoResponse(
                r.getId(),
                evaluacion.getId(),
                evaluacion.getTitulo(),
                evaluacion.getCurso().getId(),
                estudiante.getId(),
                estudiante.getNombre() + " " + estudiante.getApellido(),
                r.getNumeroIntento(),
                r.getCalificacion(),
                r.getRetroalimentacion(),
                r.getEstado(),
                r.getContenidoEntrega(),
                tardia,
                r.getFechaRegistro());
    }
}
