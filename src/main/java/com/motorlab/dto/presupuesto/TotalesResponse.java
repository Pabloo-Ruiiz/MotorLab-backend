package com.motorlab.dto.presupuesto;

import java.math.BigDecimal;

public record TotalesResponse(
        BigDecimal subtotal,
        BigDecimal ivaPorcentaje,
        BigDecimal iva,
        BigDecimal total) {
}
