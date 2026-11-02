package com.motorlab.dto.pieza;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockDeltaRequest(
        @NotNull(message = "El delta es obligatorio")
        Integer delta,
        @Size(max = 200, message = "El motivo no puede superar los 200 caracteres")
        String motivo) {

    @AssertTrue(message = "El delta no puede ser cero")
    public boolean deltaNoEsCero() {
        return delta != null && delta != 0;
    }
}
