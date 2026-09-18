package com.bitcriollo.plataforma.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecursoBibliograficoResponse {
    private Long id;
    private Long cursoId;
    private String titulo;
    private String autor;
    private Integer anioPublicacion;
    private String tipoRecurso;
    private String enlace;
    private LocalDateTime createdAt;
}