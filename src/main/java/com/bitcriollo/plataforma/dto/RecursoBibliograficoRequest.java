package com.bitcriollo.plataforma.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecursoBibliograficoRequest {

    @NotBlank
    private String titulo;

    private String autor;

    private Integer anioPublicacion;

    @NotBlank
    private String tipoRecurso;

    @NotBlank
    private String enlace;
}