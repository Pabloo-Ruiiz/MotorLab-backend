package com.motorlab.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.entity.OrdenTrabajo;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.TipoNotificacion;
import com.motorlab.entity.Presupuesto;
import com.motorlab.exception.TransicionNoPermitidaException;
import com.motorlab.repository.EventoSeguimientoRepository;
import com.motorlab.repository.PresupuestoRepository;
import com.motorlab.repository.OrdenTrabajoRepository;

@Service
public class OrdenEstadoService {

    private final TransicionesEstado transicionesEstado;
    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final SeguimientoService seguimientoService;
    private final NotificacionService notificacionService;
    private final EventoSeguimientoRepository eventoSeguimientoRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final PiezaService piezaService;

    public OrdenEstadoService(
            TransicionesEstado transicionesEstado,
            OrdenTrabajoRepository ordenTrabajoRepository,
            SeguimientoService seguimientoService,
            NotificacionService notificacionService,
            EventoSeguimientoRepository eventoSeguimientoRepository,
            PresupuestoRepository presupuestoRepository,
            PiezaService piezaService) {
        this.transicionesEstado = transicionesEstado;
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.seguimientoService = seguimientoService;
        this.notificacionService = notificacionService;
        this.eventoSeguimientoRepository = eventoSeguimientoRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.piezaService = piezaService;
    }

    @Transactional
    public OrdenTrabajo aplicarCambio(
            OrdenTrabajo orden, EstadoOrden nuevo, String descripcion, Usuario autor) {
        EstadoOrden anterior = orden.getEstado();
        if (!transicionesEstado.permitida(anterior, nuevo)) {
            throw new TransicionNoPermitidaException(
                    "No se puede pasar de " + anterior + " a " + nuevo);
        }

        if (nuevo == EstadoOrden.arreglando
                && !eventoSeguimientoRepository.existsByOrdenIdAndEstado(
                        orden.getId(), EstadoOrden.arreglando)) {
            Presupuesto presupuesto = presupuestoRepository.findByOrdenId(orden.getId())
                    .orElseThrow(() -> new TransicionNoPermitidaException(
                            "La orden no tiene un presupuesto asociado"));
            piezaService.descontarStockDePresupuesto(presupuesto.getId());
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
