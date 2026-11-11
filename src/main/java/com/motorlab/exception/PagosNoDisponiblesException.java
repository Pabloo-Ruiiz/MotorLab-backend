package com.motorlab.exception;

import org.springframework.http.HttpStatus;

public class PagosNoDisponiblesException extends ApiException {

    public PagosNoDisponiblesException() {
        this("El pago en línea no está disponible en este momento");
    }

    public PagosNoDisponiblesException(String mensaje) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "PAGOS_NO_DISPONIBLES", mensaje, null);
    }
}
