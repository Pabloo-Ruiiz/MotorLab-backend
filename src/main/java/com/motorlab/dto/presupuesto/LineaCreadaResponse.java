package com.motorlab.dto.presupuesto;

public record LineaCreadaResponse(
        LineaResponse linea,
        TotalesResponse totales,
        AvisoStockResponse avisoStock) {
}
