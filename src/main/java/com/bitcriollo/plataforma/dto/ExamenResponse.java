package com.bitcriollo.plataforma.dto;

import com.bitcriollo.plataforma.model.enums.EstadoEvaluacion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ExamenResponse {
    private Long id;
    private Long cursoId;
    private String titulo;
    private String descripcion;
    private Double pesoPorcentual;
    private EstadoEvaluacion estado;
    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaAplicacion;
    private Integer tiempoLimite;
    private Integer numeroIntentosPermitidos;
    private boolean aleatorio;
    private LocalDateTime createdAt;
}
