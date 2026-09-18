package com.bitcriollo.plataforma.dto;

import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InscripcionResponse {
    private Long id;
    private Long cursoId;
    private String cursoNombre;
    private String docenteNombre;
    private EstadoInscripcion estado;
    private LocalDateTime fechaInscripcion;
    private Double calificacionFinal;
}