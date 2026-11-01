package com.motorlab.exception;

import org.springframework.http.HttpStatus;

public class PresupuestoNoEditableException extends ApiException {

    public PresupuestoNoEditableException() {
        this("El presupuesto ya no se puede editar");
    }

    public PresupuestoNoEditableException(String mensaje) {
        super(HttpStatus.CONFLICT, "PRESUPUESTO_NO_EDITABLE", mensaje, null);
    }
}
