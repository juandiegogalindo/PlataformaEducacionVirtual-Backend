package com.bitcriollo.plataforma.dto;

import com.bitcriollo.plataforma.model.enums.EstadoInscripcion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MatriculaReporteResponse {
    private Long inscripcionId;
    private Long estudianteId;
    private String codigoEstudiantil;
    private String nombreCompleto;
    private String correo;
    private String programa;
    private Integer semestre;
    private Long cursoId;
    private String curso;
    private EstadoInscripcion estado;
    private LocalDateTime fechaInscripcion;
    private Double calificacionFinal;
}
