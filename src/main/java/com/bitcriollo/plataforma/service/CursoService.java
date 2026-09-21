package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.CursoRequest;
import com.bitcriollo.plataforma.dto.CursoResponse;
import com.bitcriollo.plataforma.model.CoordinadorAcademico;
import com.bitcriollo.plataforma.model.Curso;
import com.bitcriollo.plataforma.model.Docente;
import com.bitcriollo.plataforma.model.Foro;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.model.enums.EstadoCurso;
import com.bitcriollo.plataforma.repository.CursoRepository;
import com.bitcriollo.plataforma.repository.DocenteRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bitcriollo.plataforma.repository.ForoRepository;

import java.util.List;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final DocenteRepository docenteRepository;
    private final ForoRepository foroRepository;

    public CursoService(CursoRepository cursoRepository, DocenteRepository docenteRepository,
            ForoRepository foroRepository) {
        this.cursoRepository = cursoRepository;
        this.docenteRepository = docenteRepository;
        this.foroRepository = foroRepository;
    }

    @Transactional
    public CursoResponse crearCurso(CursoRequest request, Usuario creador) {
        Docente docente;

        if (creador instanceof Docente) {
            docente = (Docente) creador;
        } else if (creador instanceof CoordinadorAcademico) {
            if (request.getDocenteId() == null) {
                throw new IllegalArgumentException("Debes indicar el docente que dictara el curso");
            }
            docente = docenteRepository.findById(request.getDocenteId())
                    .orElseThrow(() -> new IllegalArgumentException("No existe un docente con ese id"));
        } else {
            throw new AccessDeniedException("Solo un Docente o Coordinador Academico puede crear cursos");
        }

        Curso curso = new Curso();
        curso.setNombre(request.getNombre());
        curso.setDescripcion(request.getDescripcion());
        curso.setEstado(EstadoCurso.ACTIVO);
        curso.setDocente(docente);
        curso.setCreadoPor(creador);
        curso.setFechaInicio(request.getFechaInicio());
        curso.setFechaFin(request.getFechaFin());
        curso.setCupoMaximo(request.getCupoMaximo());
        curso.setImagenPortadaUrl(request.getImagenPortadaUrl());

        cursoRepository.save(curso);

        Foro foro = new Foro();
        foro.setCurso(curso);
        foroRepository.save(foro);

        return mapearAResponse(curso);
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> listarCursos() {
        return cursoRepository.findAll().stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CursoResponse obtenerPorId(Long id) {
        return mapearAResponse(buscarCursoOLanzar(id));
    }

    @Transactional
    public CursoResponse actualizarCurso(Long id, CursoRequest request, Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(id);
        validarPermisoSobreCurso(curso, solicitante);

        curso.setNombre(request.getNombre());
        curso.setDescripcion(request.getDescripcion());
        curso.setFechaInicio(request.getFechaInicio());
        curso.setFechaFin(request.getFechaFin());
        curso.setCupoMaximo(request.getCupoMaximo());
        curso.setImagenPortadaUrl(request.getImagenPortadaUrl());

        cursoRepository.save(curso);
        return mapearAResponse(curso);
    }

    @Transactional
    public void archivarCurso(Long id, Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(id);
        validarPermisoSobreCurso(curso, solicitante);
        curso.setEstado(EstadoCurso.ARCHIVADO);
        cursoRepository.save(curso);
    }

    private Curso buscarCursoOLanzar(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un curso con id " + id));
    }

    private void validarPermisoSobreCurso(Curso curso, Usuario solicitante) {
        boolean esDocenteDelCurso = curso.getDocente().getId().equals(solicitante.getId());
        boolean esCreador = curso.getCreadoPor().getId().equals(solicitante.getId());
        boolean esCoordinador = solicitante instanceof CoordinadorAcademico;

        if (!esDocenteDelCurso && !esCreador && !esCoordinador) {
            throw new AccessDeniedException("No tienes permiso para modificar este curso");
        }
    }

    private CursoResponse mapearAResponse(Curso curso) {
        return new CursoResponse(
                curso.getId(),
                curso.getNombre(),
                curso.getDescripcion(),
                curso.getEstado(),
                curso.getDocente().getNombre() + " " + curso.getDocente().getApellido(),
                curso.getDocente().getCorreo(),
                curso.getCreadoPor().getNombre() + " " + curso.getCreadoPor().getApellido(),
                curso.getFechaInicio(),
                curso.getFechaFin(),
                curso.getCupoMaximo(),
                curso.getImagenPortadaUrl(),
                curso.getCreatedAt());
    }
}