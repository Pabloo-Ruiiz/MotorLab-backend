package com.motorlab.exception;

import org.springframework.http.HttpStatus;

public class FirmaWebhookInvalidaException extends ApiException {

    public FirmaWebhookInvalidaException() {
        super(HttpStatus.BAD_REQUEST, "FIRMA_NO_VALIDA", "La firma del aviso de pago no es válida", null);
    }
}
