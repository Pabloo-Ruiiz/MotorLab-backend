package com.motorlab.dto.pieza;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record PiezaRequest(
        @NotBlank(message = "La referencia es obligatoria")
        @Size(max = 30, message = "La referencia no puede superar los 30 caracteres")
        String referencia,
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,
        @NotNull(message = "El precio es obligatorio")
        @PositiveOrZero(message = "El precio no puede ser negativo")
        BigDecimal precio,
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock) {
}
