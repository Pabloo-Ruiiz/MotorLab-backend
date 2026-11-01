package com.motorlab.dto.common;

import java.util.List;

public record ErrorResponse(Cuerpo error) {

    public record Cuerpo(String codigo, String mensaje, List<ErrorDetalle> detalles) {
    }

    public static ErrorResponse de(String codigo, String mensaje, List<ErrorDetalle> detalles) {
        return new ErrorResponse(new Cuerpo(codigo, mensaje, detalles == null ? List.of() : List.copyOf(detalles)));
    }
}
