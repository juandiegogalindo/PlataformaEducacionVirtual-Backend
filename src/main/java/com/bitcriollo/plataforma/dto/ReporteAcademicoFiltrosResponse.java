package com.bitcriollo.plataforma.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ReporteAcademicoFiltrosResponse {
    private String tipoReporte;
    private String periodo;
    private LocalDate periodoDesde;
    private LocalDate periodoHasta;
    private String programa;
    private Long cursoId;
}
