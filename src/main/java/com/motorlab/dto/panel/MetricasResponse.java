package com.motorlab.dto.panel;

import java.math.BigDecimal;
import java.util.Map;

import com.motorlab.entity.enums.EstadoOrden;

public record MetricasResponse(
        long ordenesActivas,
        Map<EstadoOrden, Long> ordenesPorEstado,
        BigDecimal ingresosMes,
        long presupuestosSinRespuesta,
        long piezasStockBajo) {
}
