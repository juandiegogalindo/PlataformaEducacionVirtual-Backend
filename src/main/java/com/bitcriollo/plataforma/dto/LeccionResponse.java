package com.bitcriollo.plataforma.dto;

import com.bitcriollo.plataforma.model.enums.TipoContenidoLeccion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LeccionResponse {
    private Long id;
    private Long cursoId;
    private String titulo;
    private String contenido;
    private TipoContenidoLeccion tipoContenido;
    private Integer duracionMinutos;
    private Integer orden;
    private LocalDateTime createdAt;
}