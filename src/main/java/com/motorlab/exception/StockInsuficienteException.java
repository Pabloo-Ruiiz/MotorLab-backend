package com.motorlab.exception;

import java.util.List;

import org.springframework.http.HttpStatus;

import com.motorlab.dto.common.ErrorDetalle;

public class StockInsuficienteException extends ApiException {

    public StockInsuficienteException(String mensaje, List<ErrorDetalle> detalles) {
        super(HttpStatus.CONFLICT, "STOCK_INSUFICIENTE", mensaje, detalles);
    }

    public StockInsuficienteException(List<ErrorDetalle> detalles) {
        this("No hay stock suficiente para completar la operación", detalles);
    }
}
