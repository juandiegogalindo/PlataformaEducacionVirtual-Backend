package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.CursoProgresoResponse;
import com.bitcriollo.plataforma.dto.ProgresoLeccionRequest;
import com.bitcriollo.plataforma.dto.ProgresoLeccionResponse;
import com.bitcriollo.plataforma.model.*;
import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import com.bitcriollo.plataforma.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProgresoLeccionService {

    private final ProgresoLeccionRepository progresoRepository;
    private final LeccionRepository leccionRepository;
    private final CursoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;

    public ProgresoLeccionService(ProgresoLeccionRepository progresoRepository,
            LeccionRepository leccionRepository,
            CursoRepository cursoRepository,
            InscripcionRepository inscripcionRepository) {
        this.progresoRepository = progresoRepository;
        this.leccionRepository = leccionRepository;
        this.cursoRepository = cursoRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Transactional
    public ProgresoLeccionResponse marcarProgreso(Long cursoId, Long leccionId,
            ProgresoLeccionRequest request, Estudiante estudiante) {
        validarInscritoActivo(cursoId, estudiante);

        Leccion leccion = leccionRepository.findById(leccionId)
                .orElseThrow(() -> new IllegalArgumentException("No existe una leccion con id " + leccionId));
        if (!leccion.getCurso().getId().equals(cursoId)) {
            throw new IllegalArgumentException("Esa leccion no pertenece a este curso");
        }

        ProgresoLeccion progreso = progresoRepository
                .findByEstudianteIdAndLeccionId(estudiante.getId(), leccionId)
                .orElseGet(() -> {
                    ProgresoLeccion nuevo = new ProgresoLeccion();
                    nuevo.setEstudiante(estudiante);
                    nuevo.setLeccion(leccion);
                    return nuevo;
                });

        progreso.setCompletado(request.getCompletado());
        progreso.setFechaCompletado(request.getCompletado() ? LocalDateTime.now() : null);

        progresoRepository.save(progreso);
        return mapearAResponse(leccion, progreso);
    }

    @Transactional(readOnly = true)
    public CursoProgresoResponse obtenerProgresoCurso(Long cursoId, Estudiante estudiante) {
        validarInscritoActivo(cursoId, estudiante);

        List<Leccion> lecciones = leccionRepository.findByCursoIdOrderByOrdenAsc(cursoId);

        Map<Long, ProgresoLeccion> progresosPorLeccion = progresoRepository
                .findByEstudianteId(estudiante.getId()).stream()
                .collect(Collectors.toMap(p -> p.getLeccion().getId(), p -> p));

        List<ProgresoLeccionResponse> detalle = lecciones.stream()
                .map(leccion -> {
                    ProgresoLeccion progreso = progresosPorLeccion.get(leccion.getId());
                    boolean completado = progreso != null && progreso.isCompletado();
                    LocalDateTime fecha = progreso != null ? progreso.getFechaCompletado() : null;
                    return new ProgresoLeccionResponse(
                            leccion.getId(), leccion.getTitulo(), leccion.getOrden(), completado, fecha);
                })
                .toList();

        long completadas = detalle.stream().filter(ProgresoLeccionResponse::isCompletado).count();
        double porcentaje = lecciones.isEmpty() ? 0.0 : (completadas * 100.0) / lecciones.size();

        return new CursoProgresoResponse(cursoId, lecciones.size(), (int) completadas, porcentaje, detalle);
    }

    private void validarInscritoActivo(Long cursoId, Estudiante estudiante) {
        cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe un curso con id " + cursoId));

        boolean inscritoActivo = inscripcionRepository
                .findByEstudianteIdAndCursoId(estudiante.getId(), cursoId)
                .filter(i -> i.getEstado() == EstadoInscripcion.ACTIVA)
                .isPresent();

        if (!inscritoActivo) {
            throw new AccessDeniedException("No estas inscrito activamente en este curso");
        }
    }

    private ProgresoLeccionResponse mapearAResponse(Leccion leccion, ProgresoLeccion progreso) {
        return new ProgresoLeccionResponse(
                leccion.getId(),
                leccion.getTitulo(),
                leccion.getOrden(),
                progreso.isCompletado(),
                progreso.getFechaCompletado());
    }
}