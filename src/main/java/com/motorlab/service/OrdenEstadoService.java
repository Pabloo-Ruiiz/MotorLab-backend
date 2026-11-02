package com.motorlab.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.entity.OrdenTrabajo;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.TipoNotificacion;
import com.motorlab.exception.TransicionNoPermitidaException;
import com.motorlab.repository.OrdenTrabajoRepository;

@Service
public class OrdenEstadoService {

    private final TransicionesEstado transicionesEstado;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final SeguimientoService seguimientoService;
    private final NotificacionService notificacionService;

    public OrdenEstadoService(
            TransicionesEstado transicionesEstado,
            OrdenTrabajoRepository ordenTrabajoRepository,
            SeguimientoService seguimientoService,
            NotificacionService notificacionService) {
        this.transicionesEstado = transicionesEstado;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.seguimientoService = seguimientoService;
        this.notificacionService = notificacionService;
    }

    @Transactional
    public OrdenTrabajo aplicarCambio(
            OrdenTrabajo orden, EstadoOrden nuevo, String descripcion, Usuario autor) {
        EstadoOrden anterior = orden.getEstado();
        if (!transicionesEstado.permitida(anterior, nuevo)) {
            throw new TransicionNoPermitidaException(
                    "No se puede pasar de " + anterior + " a " + nuevo);
        }

        orden.setEstado(nuevo);
        if (nuevo == EstadoOrden.recibido && orden.getFechaEntrada() == null) {
            orden.setFechaEntrada(LocalDateTime.now());
        }
        OrdenTrabajo ordenGuardada = ordenTrabajoRepository.save(orden);
        seguimientoService.registrar(ordenGuardada, nuevo, descripcion, autor);

        TipoNotificacion tipo = nuevo == EstadoOrden.listo
                ? TipoNotificacion.vehiculo_listo
                : TipoNotificacion.cambio_estado;
        String mensaje = nuevo == EstadoOrden.listo
                ? "Tu vehículo está listo para recoger"
                : "La orden ha cambiado al estado " + nuevo;
        notificacionService.notificarCliente(ordenGuardada, tipo, mensaje);
        return ordenGuardada;
    }
}
