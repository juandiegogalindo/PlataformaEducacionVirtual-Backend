package com.bitcriollo.plataforma.dto;

import com.bitcriollo.plataforma.model.enums.TipoContenidoLeccion;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeccionRequest {

    @NotBlank
    private String titulo;

    private String contenido;

    @NotNull
    private TipoContenidoLeccion tipoContenido;

    private Integer duracionMinutos;

    @NotNull
    @Min(1)
    private Integer orden;
}