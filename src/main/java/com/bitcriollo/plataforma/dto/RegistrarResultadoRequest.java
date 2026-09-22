package com.bitcriollo.plataforma.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrarResultadoRequest {

    // El contenido es obligatorio para tareas y opcional para exámenes.
    private String contenido;
}