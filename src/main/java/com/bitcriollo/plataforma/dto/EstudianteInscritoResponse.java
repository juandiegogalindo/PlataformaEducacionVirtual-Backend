package com.bitcriollo.plataforma.dto;

import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EstudianteInscritoResponse {
    private Long inscripcionId;
    private Long estudianteId;
    private String nombre;
    private String correo;
    private String codigoEstudiantil;
    private String programaAcademico;
    private Integer semestre;
    private EstadoInscripcion estadoInscripcion;
    private LocalDateTime fechaInscripcion;
    private Double calificacionFinal;
    private int totalLecciones;
    private int leccionesCompletadas;
    private double porcentajeAvance;
}