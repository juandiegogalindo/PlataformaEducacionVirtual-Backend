package com.bitcriollo.plataforma.dto;

import com.bitcriollo.plataforma.model.enums.EstadoResultado;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ResultadoResponse {
    private Long id;
    private Long evaluacionId;
    private String evaluacionTitulo;
    private Long cursoId;
    private Long estudianteId;
    private String estudianteNombre;
    private Integer numeroIntento;
    private Double calificacion;
    private String retroalimentacion;
    private EstadoResultado estado;
    private String contenidoEntrega;
    private boolean entregaTardia;
    private LocalDateTime fechaRegistro;
}
