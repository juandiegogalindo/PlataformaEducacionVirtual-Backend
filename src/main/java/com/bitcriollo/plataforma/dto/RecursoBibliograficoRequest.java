package com.bitcriollo.plataforma.dto;

import com.bitcriollo.plataforma.model.enums.TipoRecurso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecursoBibliograficoRequest {

    @NotBlank
    private String titulo;

    private String autor;

    private Integer anioPublicacion;

    @NotNull
    private TipoRecurso tipoRecurso;

    @NotBlank
    private String enlace;
}