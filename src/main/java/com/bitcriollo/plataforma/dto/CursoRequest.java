package com.bitcriollo.plataforma.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CursoRequest {

    @NotBlank
    private String nombre;

    private String descripcion;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    @Min(1)
    private Integer cupoMaximo;

    private String imagenPortadaUrl;

    private Long docenteId;
}