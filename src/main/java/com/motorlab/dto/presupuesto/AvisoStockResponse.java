package com.motorlab.dto.presupuesto;

public record AvisoStockResponse(
        Integer piezaId,
        String referencia,
        Integer stock,
        java.math.BigDecimal necesarias) {
}
