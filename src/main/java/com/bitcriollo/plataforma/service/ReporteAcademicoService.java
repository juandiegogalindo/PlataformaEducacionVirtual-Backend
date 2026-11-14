package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.MatriculaReporteResponse;
import com.bitcriollo.plataforma.dto.ReporteAcademicoFiltrosResponse;
import com.bitcriollo.plataforma.dto.ReporteAcademicoResponse;
import com.bitcriollo.plataforma.model.Inscripcion;
import com.bitcriollo.plataforma.repository.InscripcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReporteAcademicoService {

    private static final Pattern PERIODO_ACADEMICO = Pattern.compile("^(\\d{4})-([12])$");
    private static final String TIPO_MATRICULAS = "MATRICULAS";

    private final InscripcionRepository inscripcionRepository;

    public ReporteAcademicoService(InscripcionRepository inscripcionRepository) {
        this.inscripcionRepository = inscripcionRepository;
    }

    @Transactional(readOnly = true)
    public ReporteAcademicoResponse consultar(String tipoReporte, String periodo, String programa, Long cursoId) {
        String tipoValidado = validarTipoReporte(tipoReporte);
        PeriodoFechas fechas = interpretarPeriodo(periodo);
        String programaNormalizado = normalizarPrograma(programa);
        if (cursoId != null && cursoId < 1) {
            throw new IllegalArgumentException("El cursoId debe ser un numero positivo");
        }

        List<MatriculaReporteResponse> resultados = inscripcionRepository
                .buscarMatriculasParaReporte(fechas.desde(), fechas.hasta(), programaNormalizado, cursoId)
                .stream()
                .map(this::mapearMatricula)
                .toList();

        ReporteAcademicoFiltrosResponse filtros = new ReporteAcademicoFiltrosResponse(
                tipoValidado, periodo.trim(), fechas.desde(), fechas.hasta(), programaNormalizado, cursoId);
        return new ReporteAcademicoResponse(filtros, resultados.size(), resultados);
    }

    private String validarTipoReporte(String tipoReporte) {
        if (tipoReporte == null || tipoReporte.isBlank()) {
            throw new IllegalArgumentException("El parametro tipoReporte es obligatorio");
        }
        if (!TIPO_MATRICULAS.equals(tipoReporte.trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("El tipo de reporte debe ser MATRICULAS");
        }
        return TIPO_MATRICULAS;
    }

    private PeriodoFechas interpretarPeriodo(String periodo) {
        if (periodo == null || periodo.isBlank()) {
            throw new IllegalArgumentException("El parametro periodo es obligatorio");
        }
        Matcher matcher = PERIODO_ACADEMICO.matcher(periodo.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("El periodo debe tener el formato YYYY-1 o YYYY-2");
        }

        int anio = Integer.parseInt(matcher.group(1));
        boolean primerSemestre = "1".equals(matcher.group(2));
        LocalDate desde = LocalDate.of(anio, primerSemestre ? 1 : 7, 1);
        LocalDate hasta = LocalDate.of(anio, primerSemestre ? 6 : 12,
                primerSemestre ? 30 : 31);
        return new PeriodoFechas(desde, hasta);
    }

    private String normalizarPrograma(String programa) {
        return programa == null || programa.isBlank() ? null : programa.trim().toLowerCase(Locale.ROOT);
    }

    private MatriculaReporteResponse mapearMatricula(Inscripcion inscripcion) {
        var estudiante = inscripcion.getEstudiante();
        var curso = inscripcion.getCurso();
        return new MatriculaReporteResponse(
                inscripcion.getId(),
                estudiante.getId(),
                estudiante.getCodigoEstudiantil(),
                estudiante.getNombre() + " " + estudiante.getApellido(),
                estudiante.getCorreo(),
                estudiante.getProgramaAcademico(),
                estudiante.getSemestre(),
                curso.getId(),
                curso.getNombre(),
                inscripcion.getEstado(),
                inscripcion.getFechaInscripcion(),
                inscripcion.getCalificacionFinal());
    }

    private record PeriodoFechas(LocalDate desde, LocalDate hasta) {
    }
}
