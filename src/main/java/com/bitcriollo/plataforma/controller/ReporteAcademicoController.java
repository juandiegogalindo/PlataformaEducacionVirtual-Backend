package com.bitcriollo.plataforma.controller;

import com.bitcriollo.plataforma.dto.ReporteAcademicoResponse;
import com.bitcriollo.plataforma.service.ExportacionReporteAcademicoService;
import com.bitcriollo.plataforma.service.ReporteAcademicoService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/reportes/academicos")
@PreAuthorize("hasRole('COORDINADORACADEMICO') or hasRole('ADMINISTRADOR')")
public class ReporteAcademicoController {

    private static final MediaType EXCEL_MEDIA_TYPE = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReporteAcademicoService reporteService;
    private final ExportacionReporteAcademicoService exportacionService;

    public ReporteAcademicoController(ReporteAcademicoService reporteService,
            ExportacionReporteAcademicoService exportacionService) {
        this.reporteService = reporteService;
        this.exportacionService = exportacionService;
    }

    @GetMapping
    public ResponseEntity<ReporteAcademicoResponse> consultar(
            @RequestParam String tipoReporte,
            @RequestParam String periodo,
            @RequestParam(required = false) String programa,
            @RequestParam(required = false) Long cursoId) {
        return ResponseEntity.ok(reporteService.consultar(tipoReporte, periodo, programa, cursoId));
    }

    @GetMapping(value = "/exportar/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam String tipoReporte,
            @RequestParam String periodo,
            @RequestParam(required = false) String programa,
            @RequestParam(required = false) Long cursoId) {
        ReporteAcademicoResponse reporte = reporteService.consultar(tipoReporte, periodo, programa, cursoId);
        byte[] archivo = exportacionService.exportarPdf(reporte);
        return respuestaArchivo(archivo, MediaType.APPLICATION_PDF,
                "reporte-academico-matriculas-" + periodo.trim() + ".pdf");
    }

    @GetMapping(value = "/exportar/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam String tipoReporte,
            @RequestParam String periodo,
            @RequestParam(required = false) String programa,
            @RequestParam(required = false) Long cursoId) {
        ReporteAcademicoResponse reporte = reporteService.consultar(tipoReporte, periodo, programa, cursoId);
        byte[] archivo = exportacionService.exportarExcel(reporte);
        return respuestaArchivo(archivo, EXCEL_MEDIA_TYPE,
                "reporte-academico-matriculas-" + periodo.trim() + ".xlsx");
    }

    private ResponseEntity<byte[]> respuestaArchivo(byte[] archivo, MediaType tipo, String nombre) {
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombre, StandardCharsets.UTF_8).build().toString())
                .body(archivo);
    }
}
