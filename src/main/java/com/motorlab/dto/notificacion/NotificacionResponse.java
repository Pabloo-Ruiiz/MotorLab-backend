package com.motorlab.dto.notificacion;

import java.time.LocalDateTime;

import com.motorlab.entity.enums.TipoNotificacion;

public record NotificacionResponse(
        Integer id,
        TipoNotificacion tipo,
        String mensaje,
        Integer ordenId,
        boolean leida,
        LocalDateTime creadaEn) {
}
