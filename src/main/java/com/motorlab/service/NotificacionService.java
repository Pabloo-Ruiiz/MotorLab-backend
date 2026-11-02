package com.motorlab.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.entity.Notificacion;
import com.motorlab.entity.OrdenTrabajo;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.Rol;
import com.motorlab.entity.enums.TipoNotificacion;
import com.motorlab.repository.NotificacionRepository;
import com.motorlab.repository.UsuarioRepository;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacionService(
            NotificacionRepository notificacionRepository,
            UsuarioRepository usuarioRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void notificar(
            Usuario destinatario, OrdenTrabajo orden, TipoNotificacion tipo, String mensaje) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(destinatario);
        notificacion.setOrden(orden);
        notificacion.setTipo(tipo);
        notificacion.setMensaje(mensaje);
        notificacion.setLeida(false);
        notificacionRepository.save(notificacion);
    }

    @Transactional
    public void notificarAdmins(OrdenTrabajo orden, TipoNotificacion tipo, String mensaje) {
        usuarioRepository.findByRolAndActivoTrue(Rol.admin)
                .forEach(administrador -> notificar(administrador, orden, tipo, mensaje));
    }

    @Transactional
    public void notificarCliente(OrdenTrabajo orden, TipoNotificacion tipo, String mensaje) {
        notificar(orden.getVehiculo().getUsuario(), orden, tipo, mensaje);
    }
}
