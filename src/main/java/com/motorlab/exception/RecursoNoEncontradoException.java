package com.motorlab.exception;

import org.springframework.http.HttpStatus;

public class RecursoNoEncontradoException extends ApiException {

    public RecursoNoEncontradoException() {
        this("Recurso no encontrado");
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", mensaje, null);
    }
}
