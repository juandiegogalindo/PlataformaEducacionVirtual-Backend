package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.CursoRequest;
import com.bitcriollo.plataforma.dto.CursoResponse;
import com.bitcriollo.plataforma.model.Administrador;
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
import java.time.LocalDate;

import java.util.List;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final DocenteRepository docenteRepository;
    private final ForoRepository foroRepository;
    private final CursoAccesoService cursoAccesoService;

    public CursoService(CursoRepository cursoRepository, DocenteRepository docenteRepository,
            ForoRepository foroRepository, CursoAccesoService cursoAccesoService) {
        this.cursoRepository = cursoRepository;
        this.docenteRepository = docenteRepository;
        this.foroRepository = foroRepository;
        this.cursoAccesoService = cursoAccesoService;
    }

    @Transactional
    public CursoResponse crearCurso(CursoRequest request, Usuario creador) {
        Docente docente;

        if (creador instanceof Docente) {
            docente = (Docente) creador;
        } else if (puedeAdministrarCursos(creador)) {
            // El coordinador y el administrador deben indicar explícitamente el docente
            // responsable del curso.
            if (request.getDocenteId() == null) {
                throw new IllegalArgumentException("Debes indicar el docente que dictara el curso");
            }
            docente = buscarDocenteActivoOLanzar(request.getDocenteId());
        } else {
            // Solo docentes, coordinadores y administradores tienen permisos para crear
            // cursos.
            throw new AccessDeniedException(
                    "Solo un Docente, Coordinador Academico o Administrador puede crear cursos");
        }

        validarSinCruceDeHorario(docente, request.getFechaInicio(), request.getFechaFin(), null);

        if (cursoRepository.existsByNombreAndDocenteIdAndFechaInicioAndFechaFinAndEstado(
                request.getNombre(), docente.getId(), request.getFechaInicio(), request.getFechaFin(),
                EstadoCurso.ACTIVO)) {
            throw new IllegalStateException(
                    "Ya existe un curso identico (mismo nombre, docente y fechas) registrado");
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

        // Cada curso nuevo inicia con un foro asociado.
        Foro foro = new Foro();
        foro.setCurso(curso);
        foroRepository.save(foro);

        return mapearAResponse(curso);
    }

        @Transactional(readOnly = true)
    public List<CursoResponse> listarCursos() {
        // El catalogo publico solo muestra los cursos activos.
        return cursoRepository.findByEstado(EstadoCurso.ACTIVO).stream()
                .map(this::mapearAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> listarCursosDeDocente(Long docenteId, EstadoCurso estado) {
        List<Curso> cursos = (estado == null)
                ? cursoRepository.findByDocenteId(docenteId)
                : cursoRepository.findByDocenteIdAndEstado(docenteId, estado);
        return cursos.stream().map(this::mapearAResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> listarCursosParaGestion(EstadoCurso estado) {
        List<Curso> cursos = (estado == null)
                ? cursoRepository.findAll()
                : cursoRepository.findByEstado(estado);
        return cursos.stream().map(this::mapearAResponse).toList();
    }

    @Transactional(readOnly = true)
    public CursoResponse obtenerPorId(Long id) {
        return mapearAResponse(buscarCursoOLanzar(id));
    }

    @Transactional
    public CursoResponse actualizarCurso(Long id, CursoRequest request, Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(id);
        validarPermisoSobreCurso(curso, solicitante);

        // Si se envia un docenteId distinto al actual, se reasigna el curso a ese
        // docente.
        Docente docente = curso.getDocente();
        if (request.getDocenteId() != null && !request.getDocenteId().equals(docente.getId())) {
            // Reasignar el docente de un curso es una decision administrativa, no del
            // docente.
            if (!puedeAdministrarCursos(solicitante)) {
                throw new AccessDeniedException(
                        "Solo un coordinador o un administrador puede reasignar el docente de un curso");
            }
            docente = buscarDocenteActivoOLanzar(request.getDocenteId());
        }

        validarSinCruceDeHorario(docente, request.getFechaInicio(), request.getFechaFin(), curso.getId());

        curso.setNombre(request.getNombre());
        curso.setDescripcion(request.getDescripcion());
        curso.setFechaInicio(request.getFechaInicio());
        curso.setFechaFin(request.getFechaFin());
        curso.setCupoMaximo(request.getCupoMaximo());
        curso.setImagenPortadaUrl(request.getImagenPortadaUrl());
        curso.setDocente(docente);

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

        @Transactional
    public CursoResponse activarCurso(Long id, Usuario solicitante) {
        Curso curso = buscarCursoOLanzar(id);
        validarPermisoSobreCurso(curso, solicitante);

        if (curso.getEstado() == EstadoCurso.ACTIVO) {
            throw new IllegalStateException("El curso ya esta activo");
        }

        // Los cursos archivados no cuentan para el cruce de fechas ni para los duplicados,
        // asi que al reactivar hay que volver a validar ambas reglas.
        validarSinCruceDeHorario(curso.getDocente(), curso.getFechaInicio(), curso.getFechaFin(), curso.getId());

        if (cursoRepository.existsByNombreAndDocenteIdAndFechaInicioAndFechaFinAndEstado(
                curso.getNombre(), curso.getDocente().getId(), curso.getFechaInicio(), curso.getFechaFin(),
                EstadoCurso.ACTIVO)) {
            throw new IllegalStateException(
                    "Ya existe un curso activo identico (mismo nombre, docente y fechas)");
        }

        curso.setEstado(EstadoCurso.ACTIVO);
        cursoRepository.save(curso);
        return mapearAResponse(curso);
    }

    private Curso buscarCursoOLanzar(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe un curso con id " + id));
    }

    private Docente buscarDocenteActivoOLanzar(Long docenteId) {
        Docente docente = docenteRepository.findById(docenteId)
                .orElseThrow(() -> new IllegalArgumentException("No existe un docente con ese id"));
        if (!docente.isActivo()) {
            throw new IllegalArgumentException("El docente indicado esta desactivado");
        }
        return docente;
    }

    private void validarSinCruceDeHorario(Docente docente, LocalDate fechaInicio, LocalDate fechaFin,
            Long cursoIdExcluir) {
        long cruces = cursoRepository.countCursosActivosConCruce(docente.getId(), cursoIdExcluir, fechaInicio,
                fechaFin);
        if (cruces > 0) {
            throw new IllegalStateException(
                    "El docente ya dicta otro curso activo cuyas fechas se cruzan con las indicadas");
        }
    }

    private void validarPermisoSobreCurso(Curso curso, Usuario solicitante) {
        // Pueden modificar el curso su docente responsable, los coordinadores y los
        // administradores.
        // El creador original ya no conserva permisos si el curso fue reasignado a otro
        // docente.
        if (!cursoAccesoService.esDocenteDelCurso(curso, solicitante) && !puedeAdministrarCursos(solicitante)) {
            throw new AccessDeniedException("No tienes permiso para modificar este curso");
        }
    }

    private boolean puedeAdministrarCursos(Usuario usuario) {
        return usuario instanceof CoordinadorAcademico || usuario instanceof Administrador;
    }

    private CursoResponse mapearAResponse(Curso curso) {
        return new CursoResponse(
                curso.getId(),
                curso.getNombre(),
                curso.getDescripcion(),
                curso.getEstado(),
                curso.getDocente().getId(),
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