package com.bitcriollo.plataforma.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CursoProgresoResponse {
    private Long cursoId;
    private int totalLecciones;
    private int leccionesCompletadas;
    private double porcentajeAvance;
    private List<ProgresoLeccionResponse> detalle;
}