package com.motorlab.dto.presupuesto;

import java.math.BigDecimal;

import com.motorlab.entity.enums.TipoLinea;

public record LineaResponse(
        Integer id,
        TipoLinea tipo,
        Integer piezaId,
        String referencia,
        String concepto,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal importe) {
}
