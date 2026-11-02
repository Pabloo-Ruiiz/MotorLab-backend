package com.motorlab.dto.presupuesto;

import java.math.BigDecimal;

import com.motorlab.entity.enums.TipoLinea;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LineaRequest(
        @NotNull(message = "El tipo es obligatorio") TipoLinea tipo,
        Integer piezaId,
        String concepto,
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero") BigDecimal cantidad,
        BigDecimal precioUnitario) {
}
