package com.bitcriollo.plataforma.exception;

// Se lanza cuando el correo o la contrasena de un intento de login no son validos.
// Se distingue de IllegalArgumentException para que el manejador global pueda responder HTTP 401.
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
