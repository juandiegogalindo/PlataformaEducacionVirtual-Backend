package com.bitcriollo.plataforma.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReporteAcademicoResponse {
    private ReporteAcademicoFiltrosResponse filtros;
    private int totalRegistros;
    private List<MatriculaReporteResponse> resultados;
}
