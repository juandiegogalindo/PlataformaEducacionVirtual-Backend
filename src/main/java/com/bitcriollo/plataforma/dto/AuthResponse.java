package com.bitcriollo.plataforma.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tipo;
    private String correo;
    private String rol;
}