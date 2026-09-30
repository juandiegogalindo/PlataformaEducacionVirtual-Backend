package com.bitcriollo.plataforma.security;

import com.bitcriollo.plataforma.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

// Punto de entrada de autenticacion: sin este componente, Spring Security responde 403 tanto para
// peticiones sin token como para accesos sin el rol requerido. Con este componente, solo las
// peticiones sin autenticar (token ausente, invalido o expirado) reciben HTTP 401, conforme al
// estandar 2.1.4 del Plan de Implementacion.
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public AuthEntryPointJwt(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(),
                "Debes iniciar sesion para acceder a este recurso");

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
