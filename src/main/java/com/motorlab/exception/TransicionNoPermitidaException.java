package com.motorlab.exception;

import org.springframework.http.HttpStatus;

public class TransicionNoPermitidaException extends ApiException {

    public TransicionNoPermitidaException() {
        this("La transición de estado no está permitida");
    }

    public TransicionNoPermitidaException(String mensaje) {
        super(HttpStatus.CONFLICT, "TRANSICION_NO_PERMITIDA", mensaje, null);
    }
}
