package com.bitcriollo.plataforma.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocenteResumenResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String correo;
    private String especialidad;
}