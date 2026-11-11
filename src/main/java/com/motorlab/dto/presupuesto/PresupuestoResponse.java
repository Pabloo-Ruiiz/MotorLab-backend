package com.motorlab.dto.presupuesto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.motorlab.entity.enums.EstadoPago;
import com.motorlab.entity.enums.EstadoPresupuesto;
import com.motorlab.entity.enums.MetodoPago;

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
        LocalDateTime pagadoEn,
        MetodoPago metodoPago,
        EstadoPago estadoPago,
        List<AvisoStockResponse> avisosStock) {
}
