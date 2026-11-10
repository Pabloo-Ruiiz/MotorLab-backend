package com.motorlab.exception;

import org.springframework.http.HttpStatus;

public class EmailNoVerificadoException extends ApiException {

    public EmailNoVerificadoException() {
        super(
                HttpStatus.FORBIDDEN,
                "EMAIL_NO_VERIFICADO",
                "Debes confirmar tu correo antes de iniciar sesión. Revisa tu bandeja de entrada.",
                null);
    }
}
