package com.bitcriollo.plataforma.dto;

import com.bitcriollo.plataforma.model.enums.TipoDocumento;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroRequest {

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
    private String codigoEstudiantil;

    @NotNull
    private TipoDocumento tipoDocumento;

    @NotBlank
    private String numeroDocumento;

    @NotBlank
    private String programaAcademico;

    @NotNull
    @Min(1)
    private Integer semestre;
}