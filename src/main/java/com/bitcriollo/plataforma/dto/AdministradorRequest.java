package com.bitcriollo.plataforma.dto;

import com.bitcriollo.plataforma.model.enums.NivelAccesoAdmin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdministradorRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @NotBlank
    @Email
    private String correo;

    @NotBlank
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    private String contrasena;

    @NotBlank
    private String codigo;

    @NotBlank
    private String cargo;

    private NivelAccesoAdmin nivelAcceso;
}