package com.bitcriollo.plataforma.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProgresoLeccionResponse {
    private Long leccionId;
    private String leccionTitulo;
    private Integer orden;
    private boolean completado;
    private LocalDateTime fechaCompletado;
}