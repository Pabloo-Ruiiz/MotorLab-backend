package com.motorlab.exception;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.motorlab.dto.common.ErrorDetalle;

public abstract class ApiException extends RuntimeException {

    private final HttpStatus estado;
    private final String codigo;
    private final List<ErrorDetalle> detalles;

    protected ApiException(HttpStatus estado, String codigo, String mensaje, List<ErrorDetalle> detalles) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
        this.detalles = detalles == null ? List.of() : List.copyOf(detalles);
    }

    public HttpStatus getEstado() {
        return estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public List<ErrorDetalle> getDetalles() {
        return detalles;
    }
}
