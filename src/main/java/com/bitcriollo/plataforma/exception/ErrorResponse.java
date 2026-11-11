package com.bitcriollo.plataforma.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String mensaje;
    private String codigo;
    private java.util.List<String> errores;
    private String correlationId;

    public ErrorResponse(LocalDateTime timestamp, int status, String mensaje) {
        this(timestamp, status, mensaje, status == 400 ? "VALIDATION_ERROR" : "REQUEST_ERROR",
                java.util.List.of(), null);
    }

    public ErrorResponse(LocalDateTime timestamp, int status, String mensaje, String codigo,
            java.util.List<String> errores) {
        this(timestamp, status, mensaje, codigo, errores, null);
    }
}