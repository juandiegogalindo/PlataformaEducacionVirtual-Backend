package com.bitcriollo.plataforma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambiarContrasenaRequest {

    @NotBlank
    private String contrasenaActual;

    @NotBlank
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    private String contrasenaNueva;

    @NotBlank
    private String confirmarContrasenaNueva;
}