package com.motorlab.exception;

import org.springframework.http.HttpStatus;

public class SinPermisoException extends ApiException {

    public SinPermisoException() {
        this("No tiene permiso para realizar esta operación");
    }

    public SinPermisoException(String mensaje) {
        super(HttpStatus.FORBIDDEN, "SIN_PERMISO", mensaje, null);
    }
}
