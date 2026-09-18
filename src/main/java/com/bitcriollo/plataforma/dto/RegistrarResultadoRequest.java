package com.bitcriollo.plataforma.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrarResultadoRequest {

    // Obligatorio para tareas (texto o enlace de la entrega); opcional para examenes
    private String contenido;
}
