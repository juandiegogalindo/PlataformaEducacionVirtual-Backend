package com.bitcriollo.plataforma.dto;

import com.bitcriollo.plataforma.model.enums.EstadoEvaluacion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EvaluacionResponse {
    private Long id;
    private Long cursoId;
    private String tipo;
    private String titulo;
    private String descripcion;
    private Double pesoPorcentual;
    private EstadoEvaluacion estado;
    private LocalDateTime fechaPublicacion;
    private LocalDateTime createdAt;
}
