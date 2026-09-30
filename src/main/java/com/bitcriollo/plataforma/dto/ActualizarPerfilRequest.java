package com.bitcriollo.plataforma.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarPerfilRequest {

    private String nombre;

    private String apellido;

    private String telefono;

    private String fotoPerfilUrl;
}