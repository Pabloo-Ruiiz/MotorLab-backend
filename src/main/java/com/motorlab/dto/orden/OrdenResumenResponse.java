package com.motorlab.dto.orden;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.EstadoPago;
import com.motorlab.entity.enums.EstadoPresupuesto;
import com.motorlab.entity.enums.TipoServicio;
import com.motorlab.entity.enums.Urgencia;

public record OrdenResumenResponse(
        Integer ordenId,
        EstadoOrden estado,
        int progreso,
        Urgencia urgencia,
        TipoServicio tipoServicio,
        String matricula,
        String marca,
        String modelo,
        String clienteNombre,
        String mecanicoNombre,
        String elevador,
        LocalDateTime fechaEntrada,
        LocalDateTime fechaEstimada,
        EstadoPresupuesto presupuestoEstado,
        BigDecimal presupuestoTotal,
        EstadoPago presupuestoEstadoPago) {
}
