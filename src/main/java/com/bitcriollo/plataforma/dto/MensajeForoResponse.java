package com.bitcriollo.plataforma.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MensajeForoResponse {
    private Long id;
    private String autorNombre;
    private String autorRol;
    private String contenido;
    private boolean editado;
    private LocalDateTime fechaPublicacion;
    private List<MensajeForoResponse> respuestas;
}