package com.motorlab.dto.presupuesto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.motorlab.entity.enums.EstadoPresupuesto;

public record PresupuestoResponse(
        Integer id,
        Integer ordenId,
        EstadoPresupuesto estado,
        List<LineaResponse> lineas,
        BigDecimal subtotal,
        BigDecimal ivaPorcentaje,
        BigDecimal iva,
        BigDecimal total,
        BigDecimal tiempoEstimadoHoras,
        Integer garantiaMeses,
        String motivoRechazo,
        LocalDateTime enviadoEn,
        LocalDateTime respondidoEn,
        List<AvisoStockResponse> avisosStock) {
}
