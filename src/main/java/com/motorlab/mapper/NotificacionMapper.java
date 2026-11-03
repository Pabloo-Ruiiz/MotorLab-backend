package com.motorlab.mapper;

import org.springframework.stereotype.Component;

import com.motorlab.dto.notificacion.NotificacionResponse;
import com.motorlab.entity.Notificacion;

@Component
public class NotificacionMapper {

    public NotificacionResponse aResponse(Notificacion notificacion) {
        return new NotificacionResponse(
                notificacion.getId(),
                notificacion.getTipo(),
                notificacion.getMensaje(),
                notificacion.getOrden() == null ? null : notificacion.getOrden().getId(),
                notificacion.isLeida(),
                notificacion.getCreadaEn());
    }
}
