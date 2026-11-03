package com.motorlab.dto.orden;

import java.time.LocalDateTime;

import com.motorlab.entity.enums.EstadoOrden;

public record EventoResponse(
        EstadoOrden estado,
        String descripcion,
        LocalDateTime fechaHora) {
}
