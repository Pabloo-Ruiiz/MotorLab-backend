package com.motorlab.dto.presupuesto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record PresupuestoUpdateRequest(
        @Positive(message = "El tiempo estimado debe ser mayor que cero")
        BigDecimal tiempoEstimadoHoras,
        @Max(value = 120, message = "La garantía no puede superar 120 meses")
        @DecimalMin(value = "0", message = "La garantía no puede ser negativa")
        Integer garantiaMeses) {
}
