package com.motorlab.exception;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.motorlab.dto.common.ErrorDetalle;

public class SolicitudInvalidaException extends ApiException {

    public SolicitudInvalidaException(String mensaje, List<ErrorDetalle> detalles) {
        super(HttpStatus.BAD_REQUEST, "VALIDACION", mensaje, detalles);
    }

    public SolicitudInvalidaException(String mensaje) {
        this(mensaje, null);
    }
}
