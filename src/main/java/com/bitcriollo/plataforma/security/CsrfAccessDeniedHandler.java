package com.bitcriollo.plataforma.security;

import com.bitcriollo.plataforma.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class CsrfAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CsrfAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        if (!(exception instanceof MissingCsrfTokenException)
                && !(exception instanceof InvalidCsrfTokenException)) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return;
        }
        String correlationId = response.getHeader("X-Correlation-Id");
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(), HttpStatus.FORBIDDEN.value(),
                "La validacion CSRF es obligatoria o no es valida", "CSRF_INVALID", List.of(), correlationId);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
