package com.motorlab.dto.presupuesto;

import java.math.BigDecimal;

import com.motorlab.entity.enums.EstadoPresupuesto;

public record PresupuestoResumenResponse(
        Integer id,
        EstadoPresupuesto estado,
        BigDecimal total) {
}
