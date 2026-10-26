package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.CursoRequest;
import com.bitcriollo.plataforma.dto.CursoResponse;
import com.bitcriollo.plataforma.dto.HorarioRequest;
import com.bitcriollo.plataforma.dto.HorarioResponse;
import com.bitcriollo.plataforma.model.Administrador;
import com.bitcriollo.plataforma.model.CoordinadorAcademico;
import com.bitcriollo.plataforma.model.Curso;
import com.bitcriollo.plataforma.model.Docente;
import com.bitcriollo.plataforma.model.Foro;
import com.bitcriollo.plataforma.model.HorarioCurso;
import com.bitcriollo.plataforma.model.Usuario;
import com.bitcriollo.plataforma.model.enums.EstadoCurso;
import com.bitcriollo.plataforma.repository.CursoRepository;
import com.bitcriollo.plataforma.repository.DocenteRepository;
import com.bitcriollo.plataforma.repository.ForoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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
            // El coordinador y el administrador deben indicar explícitamente el docente responsable del curso.
            if (request.getDocenteId() == null) {
                throw new IllegalArgumentException("Debes indicar el docente que dictara el curso");
            }
            docente = buscarDocenteActivoOLanzar(request.getDocenteId());
        } else {
            // Solo docentes, coordinadores y administradores tienen permisos para crear cursos.
            throw new AccessDeniedException(
                    "Solo un Docente, Coordinador Academico o Administrador puede crear cursos");
        }

        // Todo curso nuevo debe tener al menos una sesion semanal.
        List<HorarioCurso> horarios = construirHorarios(request.getHorarios());

        validarSinCruceDeHorario(docente, request.getFechaInicio(), request.getFechaFin(), horarios, null);

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
        asignarHorarios(curso, horarios);

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

        // Si se envia un docenteId distinto al actual, se reasigna el curso a ese docente.
        Docente docente = curso.getDocente();
        if (request.getDocenteId() != null && !request.getDocenteId().equals(docente.getId())) {
            // Reasignar el docente de un curso es una decision administrativa, no del docente.
            if (!puedeAdministrarCursos(solicitante)) {
                throw new AccessDeniedException(
                        "Solo un coordinador o un administrador puede reasignar el docente de un curso");
            }
            docente = buscarDocenteActivoOLanzar(request.getDocenteId());
        }

        // Si no se envian horarios se conservan los actuales; si se envian, reemplazan a los anteriores.
        List<HorarioCurso> horariosNuevos = (request.getHorarios() != null)
                ? construirHorarios(request.getHorarios())
                : null;
        List<HorarioCurso> horariosVigentes = (horariosNuevos != null) ? horariosNuevos : curso.getHorarios();

        validarSinCruceDeHorario(docente, request.getFechaInicio(), request.getFechaFin(), horariosVigentes,
                curso.getId());

        curso.setNombre(request.getNombre());
        curso.setDescripcion(request.getDescripcion());
        curso.setFechaInicio(request.getFechaInicio());
        curso.setFechaFin(request.getFechaFin());
        curso.setCupoMaximo(request.getCupoMaximo());
        curso.setImagenPortadaUrl(request.getImagenPortadaUrl());
        curso.setDocente(docente);
        if (horariosNuevos != null) {
            curso.getHorarios().clear();
            asignarHorarios(curso, horariosNuevos);
        }

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

        // Los cursos archivados no cuentan para el cruce de horarios: al reactivar hay que volver a validarlo.
        validarSinCruceDeHorario(curso.getDocente(), curso.getFechaInicio(), curso.getFechaFin(),
                curso.getHorarios(), curso.getId());

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

    // Convierte y valida las sesiones recibidas: al menos una, con hora de inicio anterior a la de fin
    // y sin que se solapen entre si dentro del mismo curso.
    private List<HorarioCurso> construirHorarios(List<HorarioRequest> solicitudes) {
        if (solicitudes == null || solicitudes.isEmpty()) {
            throw new IllegalArgumentException("Debes indicar al menos una sesion de horario para el curso");
        }

        List<HorarioCurso> horarios = new ArrayList<>();
        for (HorarioRequest solicitud : solicitudes) {
            if (!solicitud.getHoraInicio().isBefore(solicitud.getHoraFin())) {
                throw new IllegalArgumentException(
                        "En cada sesion la hora de inicio debe ser anterior a la hora de fin");
            }
            HorarioCurso horario = new HorarioCurso();
            horario.setDiaSemana(solicitud.getDiaSemana());
            horario.setHoraInicio(solicitud.getHoraInicio());
            horario.setHoraFin(solicitud.getHoraFin());
            horarios.add(horario);
        }

        for (int i = 0; i < horarios.size(); i++) {
            for (int j = i + 1; j < horarios.size(); j++) {
                if (sesionesSeCruzan(horarios.get(i), horarios.get(j))) {
                    throw new IllegalArgumentException(
                            "Las sesiones de un mismo dia no pueden solaparse entre si");
                }
            }
        }
        return horarios;
    }

    private void asignarHorarios(Curso curso, List<HorarioCurso> horarios) {
        for (HorarioCurso horario : horarios) {
            horario.setCurso(curso);
            curso.getHorarios().add(horario);
        }
    }

    // Dos sesiones se cruzan si caen el mismo dia y sus horas se solapan.
    // Que una termine justo cuando empieza la otra no cuenta como cruce.
    private boolean sesionesSeCruzan(HorarioCurso a, HorarioCurso b) {
        return a.getDiaSemana() == b.getDiaSemana()
                && a.getHoraInicio().isBefore(b.getHoraFin())
                && b.getHoraInicio().isBefore(a.getHoraFin());
    }

    // Un docente no puede dictar dos cursos activos que coincidan en fechas Y tengan una sesion el mismo
    // dia con horas que se cruzan. Un curso sin horario definido (cursos anteriores) no genera conflictos.
    private void validarSinCruceDeHorario(Docente docente, LocalDate fechaInicio, LocalDate fechaFin,
            List<HorarioCurso> horarios, Long cursoIdExcluir) {
        if (horarios.isEmpty()) {
            return;
        }

        List<Curso> candidatos = cursoRepository.findCursosActivosConFechasCruzadas(
                docente.getId(), cursoIdExcluir, fechaInicio, fechaFin);

        for (Curso otro : candidatos) {
            for (HorarioCurso nueva : horarios) {
                for (HorarioCurso existente : otro.getHorarios()) {
                    if (sesionesSeCruzan(nueva, existente)) {
                        throw new IllegalStateException("El docente ya dicta el curso '" + otro.getNombre()
                                + "' los " + existente.getDiaSemana() + " de " + existente.getHoraInicio()
                                + " a " + existente.getHoraFin() + ", que se cruza con la sesion indicada");
                    }
                }
            }
        }
    }

    private void validarPermisoSobreCurso(Curso curso, Usuario solicitante) {
        // Pueden modificar el curso su docente responsable, los coordinadores y los administradores.
        // El creador original ya no conserva permisos si el curso fue reasignado a otro docente.
        if (!cursoAccesoService.esDocenteDelCurso(curso, solicitante) && !puedeAdministrarCursos(solicitante)) {
            throw new AccessDeniedException("No tienes permiso para modificar este curso");
        }
    }

    private boolean puedeAdministrarCursos(Usuario usuario) {
        return usuario instanceof CoordinadorAcademico || usuario instanceof Administrador;
    }

    private CursoResponse mapearAResponse(Curso curso) {
        List<HorarioResponse> horarios = curso.getHorarios().stream()
                .sorted(Comparator.comparing(HorarioCurso::getDiaSemana)
                        .thenComparing(HorarioCurso::getHoraInicio))
                .map(h -> new HorarioResponse(h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin()))
                .toList();

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
                curso.getCreatedAt(),
                horarios);
    }
}