package com.motorlab.exception;

import org.springframework.http.HttpStatus;

public class TokenVerificacionInvalidoException extends ApiException {

    public TokenVerificacionInvalidoException() {
        super(
                HttpStatus.BAD_REQUEST,
                "TOKEN_INVALIDO",
                "El enlace no es válido o ha caducado. Inicia sesión para pedir uno nuevo.",
                null);
    }
}
