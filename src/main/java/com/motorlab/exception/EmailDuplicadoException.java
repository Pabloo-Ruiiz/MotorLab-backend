package com.motorlab.exception;

import org.springframework.http.HttpStatus;

public class EmailDuplicadoException extends ApiException {

    public EmailDuplicadoException() {
        this("El email ya está registrado");
    }

    public EmailDuplicadoException(String mensaje) {
        super(HttpStatus.CONFLICT, "EMAIL_DUPLICADO", mensaje, null);
    }
}
