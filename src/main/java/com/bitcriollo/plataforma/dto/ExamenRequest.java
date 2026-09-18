package com.bitcriollo.plataforma.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ExamenRequest {

    @NotBlank
    private String titulo;

    private String descripcion;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @DecimalMax("100.0")
    private Double pesoPorcentual;

    @NotNull
    private LocalDateTime fechaAplicacion;

    @NotNull
    @Min(1)
    private Integer tiempoLimite;

    @Min(1)
    private Integer numeroIntentosPermitidos;

    private boolean aleatorio;
}
