package com.motorlab.exception;

import org.springframework.http.HttpStatus;

public class MatriculaDuplicadaException extends ApiException {

    public MatriculaDuplicadaException() {
        this("La matrícula ya está registrada");
    }

    public MatriculaDuplicadaException(String mensaje) {
        super(HttpStatus.CONFLICT, "MATRICULA_DUPLICADA", mensaje, null);
    }
}
