package com.bitcriollo.plataforma.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MensajeForoRequest {

    @NotBlank
    private String contenido;

    private Long mensajePadreId;
}