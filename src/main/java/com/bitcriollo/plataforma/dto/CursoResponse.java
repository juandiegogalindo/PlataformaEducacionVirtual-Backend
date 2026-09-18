package com.bitcriollo.plataforma.dto;

import com.bitcriollo.plataforma.model.enums.EstadoCurso;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CursoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private EstadoCurso estado;
    private String docenteNombre;
    private String docenteCorreo;
    private String creadoPorNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer cupoMaximo;
    private String imagenPortadaUrl;
    private LocalDateTime createdAt;
}