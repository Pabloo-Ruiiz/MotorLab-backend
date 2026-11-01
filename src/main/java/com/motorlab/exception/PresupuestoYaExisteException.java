package com.motorlab.exception;

import org.springframework.http.HttpStatus;

public class PresupuestoYaExisteException extends ApiException {

    public PresupuestoYaExisteException() {
        this("La orden ya tiene un presupuesto");
    }

    public PresupuestoYaExisteException(String mensaje) {
        super(HttpStatus.CONFLICT, "PRESUPUESTO_YA_EXISTE", mensaje, null);
    }
}
