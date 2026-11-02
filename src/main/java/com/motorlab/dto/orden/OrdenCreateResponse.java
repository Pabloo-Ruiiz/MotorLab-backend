package com.motorlab.dto.orden;

import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.Urgencia;

public record OrdenCreateResponse(
        Integer id,
        EstadoOrden estado,
        Urgencia urgencia,
        String mensaje) {
}
