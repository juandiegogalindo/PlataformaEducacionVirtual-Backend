package com.bitcriollo.plataforma.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalificarResultadoRequest {

    @NotNull
    @DecimalMin("0.0")
    private Double calificacion;

    private String retroalimentacion;
}
