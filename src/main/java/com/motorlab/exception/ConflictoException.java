package com.motorlab.exception;

import org.springframework.http.HttpStatus;

public class ConflictoException extends ApiException {

    public ConflictoException() {
        this("La operación entra en conflicto con el estado actual");
    }

    public ConflictoException(String mensaje) {
        super(HttpStatus.CONFLICT, "CONFLICTO", mensaje, null);
    }
}
