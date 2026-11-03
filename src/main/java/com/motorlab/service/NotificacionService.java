package com.motorlab.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.dto.common.PageResponse;
import com.motorlab.dto.notificacion.NotificacionResponse;
import com.motorlab.entity.Notificacion;
import com.motorlab.entity.OrdenTrabajo;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.Rol;
import com.motorlab.entity.enums.TipoNotificacion;
import com.motorlab.exception.RecursoNoEncontradoException;
import com.motorlab.mapper.NotificacionMapper;
import com.motorlab.repository.NotificacionRepository;
import com.motorlab.repository.UsuarioRepository;
import com.motorlab.security.SecurityUtils;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionMapper notificacionMapper;

    public NotificacionService(
            NotificacionRepository notificacionRepository,
            UsuarioRepository usuarioRepository,
            NotificacionMapper notificacionMapper) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionMapper = notificacionMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificacionResponse> listar(
            Boolean leida, Integer pagina, Integer limite) {
        Pageable pageable = PageResponse.pageable(pagina, limite);
        Integer usuarioId = SecurityUtils.idActual();
        Page<Notificacion> notificaciones = leida == null
                ? notificacionRepository.findByUsuarioIdOrderByCreadaEnDescIdDesc(usuarioId, pageable)
                : notificacionRepository.findByUsuarioIdAndLeidaOrderByCreadaEnDescIdDesc(
                        usuarioId, leida, pageable);
        return PageResponse.de(notificaciones.map(notificacionMapper::aResponse));
    }

    @Transactional
    public NotificacionResponse marcarLeida(Integer id) {
        Notificacion notificacion = notificacionRepository.findByIdAndUsuarioId(
                id, SecurityUtils.idActual())
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada"));
        notificacion.setLeida(true);
        return notificacionMapper.aResponse(notificacionRepository.save(notificacion));
    }

    @Transactional
    public int marcarTodas() {
        return notificacionRepository.marcarTodasLeidas(SecurityUtils.idActual());
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas() {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(SecurityUtils.idActual());
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
