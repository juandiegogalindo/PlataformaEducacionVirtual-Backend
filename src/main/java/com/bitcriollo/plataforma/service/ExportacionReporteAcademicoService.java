package com.bitcriollo.plataforma.service;

import com.bitcriollo.plataforma.dto.MatriculaReporteResponse;
import com.bitcriollo.plataforma.dto.ReporteAcademicoResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExportacionReporteAcademicoService {

    private static final String[] ENCABEZADOS = {
            "Inscripcion ID", "Estudiante ID", "Codigo estudiantil", "Estudiante", "Correo",
            "Programa", "Semestre", "Curso ID", "Curso", "Estado", "Fecha inscripcion", "Calificacion final"
    };

    public byte[] exportarExcel(ReporteAcademicoResponse reporte) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Matriculas");
            Font tituloFont = workbook.createFont();
            tituloFont.setBold(true);
            tituloFont.setFontHeightInPoints((short) 15);
            CellStyle tituloStyle = workbook.createCellStyle();
            tituloStyle.setFont(tituloFont);

            Font encabezadoFont = workbook.createFont();
            encabezadoFont.setBold(true);
            CellStyle encabezadoStyle = workbook.createCellStyle();
            encabezadoStyle.setFont(encabezadoFont);

            Row titulo = sheet.createRow(0);
            Cell tituloCell = titulo.createCell(0);
            tituloCell.setCellValue("Reporte academico de matriculas");
            tituloCell.setCellStyle(tituloStyle);
            sheet.createRow(1).createCell(0).setCellValue("Tipo de reporte: " + reporte.getFiltros().getTipoReporte());
            sheet.createRow(2).createCell(0).setCellValue("Periodo academico: " + reporte.getFiltros().getPeriodo()
                    + " (" + reporte.getFiltros().getPeriodoDesde() + " a " + reporte.getFiltros().getPeriodoHasta() + ")");
            sheet.createRow(3).createCell(0).setCellValue("Programa: " + valorFiltro(reporte.getFiltros().getPrograma()));
            sheet.createRow(4).createCell(0).setCellValue("Curso ID: " + valorFiltro(reporte.getFiltros().getCursoId()));
            sheet.createRow(5).createCell(0).setCellValue("Total de registros: " + reporte.getTotalRegistros());

            Row encabezados = sheet.createRow(7);
            for (int columna = 0; columna < ENCABEZADOS.length; columna++) {
                Cell celda = encabezados.createCell(columna);
                celda.setCellValue(ENCABEZADOS[columna]);
                celda.setCellStyle(encabezadoStyle);
            }

            int numeroFila = 8;
            for (MatriculaReporteResponse matricula : reporte.getResultados()) {
                Row fila = sheet.createRow(numeroFila++);
                String[] valores = valores(matricula);
                for (int columna = 0; columna < valores.length; columna++) {
                    fila.createCell(columna).setCellValue(valores[columna]);
                }
            }

            for (int columna = 0; columna < ENCABEZADOS.length; columna++) {
                sheet.autoSizeColumn(columna);
                sheet.setColumnWidth(columna, Math.min(sheet.getColumnWidth(columna), 15000));
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel", ex);
        }
    }

    public byte[] exportarPdf(ReporteAcademicoResponse reporte) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            List<String> lineas = crearLineasPdf(reporte);
            for (int inicio = 0; inicio < lineas.size(); inicio += 48) {
                PDPage pagina = new PDPage();
                document.addPage(pagina);
                try (PDPageContentStream contenido = new PDPageContentStream(document, pagina)) {
                    contenido.beginText();
                    contenido.newLineAtOffset(36, 755);
                    for (int indice = inicio; indice < Math.min(inicio + 48, lineas.size()); indice++) {
                        contenido.setFont(indice == 0 ? new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                                : new PDType1Font(Standard14Fonts.FontName.HELVETICA), indice == 0 ? 12 : 8);
                        contenido.showText(paraWinAnsi(lineas.get(indice)));
                        contenido.newLineAtOffset(0, -14);
                    }
                    contenido.endText();
                }
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar el archivo PDF", ex);
        }
    }

    private List<String> crearLineasPdf(ReporteAcademicoResponse reporte) {
        var filtros = reporte.getFiltros();
        List<String> lineas = new ArrayList<>();
        lineas.add("Reporte academico de matriculas");
        lineas.add("Tipo de reporte: " + filtros.getTipoReporte());
        lineas.add("Periodo academico: " + filtros.getPeriodo() + " (" + filtros.getPeriodoDesde() + " a "
                + filtros.getPeriodoHasta() + ")");
        lineas.add("Programa: " + valorFiltro(filtros.getPrograma()));
        lineas.add("Curso ID: " + valorFiltro(filtros.getCursoId()));
        lineas.add("Total de registros: " + reporte.getTotalRegistros());
        lineas.add("Inscripcion | Codigo | Estudiante | Programa | Curso | Estado | Fecha");
        if (reporte.getResultados().isEmpty()) {
            lineas.add("No se encontraron matriculas para los filtros aplicados.");
        } else {
            for (MatriculaReporteResponse matricula : reporte.getResultados()) {
                String linea = matricula.getInscripcionId() + " | " + valor(matricula.getCodigoEstudiantil()) + " | "
                        + valor(matricula.getNombreCompleto()) + " | " + valor(matricula.getPrograma()) + " | "
                        + valor(matricula.getCurso()) + " | " + matricula.getEstado() + " | "
                        + (matricula.getFechaInscripcion() == null ? "" : matricula.getFechaInscripcion());
                lineas.add(limitar(linea, 125));
            }
        }
        return lineas;
    }

    private String[] valores(MatriculaReporteResponse matricula) {
        return new String[] {
                valor(matricula.getInscripcionId()), valor(matricula.getEstudianteId()),
                valor(matricula.getCodigoEstudiantil()), valor(matricula.getNombreCompleto()),
                valor(matricula.getCorreo()), valor(matricula.getPrograma()), valor(matricula.getSemestre()),
                valor(matricula.getCursoId()), valor(matricula.getCurso()), valor(matricula.getEstado()),
                valor(matricula.getFechaInscripcion()), valor(matricula.getCalificacionFinal())
        };
    }

    private String valorFiltro(Object valor) {
        return valor == null ? "Todos" : valor.toString();
    }

    private String valor(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    private String limitar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 3) + "...";
    }

    private String paraWinAnsi(String texto) {
        StringBuilder compatible = new StringBuilder(texto.length());
        texto.codePoints().forEach(caracter -> compatible.appendCodePoint(
                (caracter >= 32 && caracter <= 126) || (caracter >= 160 && caracter <= 255) ? caracter : '?'));
        return compatible.toString();
    }
}
