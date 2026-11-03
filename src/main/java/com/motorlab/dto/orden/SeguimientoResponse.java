package com.motorlab.dto.orden;

import java.time.LocalDateTime;
import java.util.List;

import com.motorlab.entity.enums.EstadoOrden;

public record SeguimientoResponse(
        Integer ordenId,
        String vehiculo,
        EstadoOrden estado,
        int progreso,
        LocalDateTime listoEstimado,
        List<EventoResponse> eventos) {
}
