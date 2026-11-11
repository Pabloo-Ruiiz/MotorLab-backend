package com.motorlab.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.motorlab.dto.pago.SesionPagoResponse;
import com.motorlab.entity.OrdenTrabajo;
import com.motorlab.entity.Presupuesto;
import com.motorlab.entity.enums.EstadoPago;
import com.motorlab.entity.enums.EstadoPresupuesto;
import com.motorlab.entity.enums.MetodoPago;
import com.motorlab.entity.enums.TipoNotificacion;
import com.motorlab.exception.ConflictoException;
import com.motorlab.exception.PagosNoDisponiblesException;
import com.motorlab.exception.RecursoNoEncontradoException;
import com.motorlab.exception.SolicitudInvalidaException;
import com.motorlab.exception.TransicionNoPermitidaException;
import com.motorlab.repository.PresupuestoRepository;
import com.motorlab.security.SecurityUtils;
import com.motorlab.service.pago.PasarelaPago;
import com.motorlab.service.pago.PasarelaPago.NuevaSesionPago;
import com.motorlab.service.pago.PasarelaPago.PagoCompletado;
import com.motorlab.service.pago.PasarelaPago.SesionPago;

@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);
    private static final String MONEDA = "eur";

    private final PresupuestoRepository presupuestoRepository;
    private final NotificacionService notificacionService;
    private final PasarelaPago pasarela;
    private final String urlFrontend;

    public PagoService(
            PresupuestoRepository presupuestoRepository,
            NotificacionService notificacionService,
            PasarelaPago pasarela,
            @Value("${app.frontend-url}") String urlFrontend) {
        this.presupuestoRepository = presupuestoRepository;
        this.notificacionService = notificacionService;
        this.pasarela = pasarela;
        this.urlFrontend = urlFrontend;
    }

    @Transactional
    public SesionPagoResponse iniciar(Integer presupuestoId) {
        if (!pasarela.configurada()) {
            throw new PagosNoDisponiblesException();
        }
        Presupuesto presupuesto = obtenerDelCliente(presupuestoId);
        if (presupuesto.getPagadoEn() != null) {
            throw new ConflictoException("Este presupuesto ya está pagado");
        }
        if (presupuesto.getEstado() != EstadoPresupuesto.autorizado) {
            throw new TransicionNoPermitidaException("Solo se puede pagar un presupuesto autorizado");
        }

        // El importe sale siempre del presupuesto guardado, nunca de lo que envía el navegador
        long importe = enCentimos(presupuesto.getTotal());
        if (importe <= 0) {
            throw new SolicitudInvalidaException("El presupuesto no tiene importe a pagar");
        }
        OrdenTrabajo orden = presupuesto.getOrden();
        String vuelta = UriComponentsBuilder.fromUriString(urlFrontend)
                .path("/cliente/ordenes/{orden}/presupuesto")
                .buildAndExpand(orden.getId())
                .toUriString();

        SesionPago sesion = pasarela.crearSesion(new NuevaSesionPago(
                presupuesto.getId(),
                importe,
                "Presupuesto P-%d · %s %s".formatted(
                        presupuesto.getId(),
                        orden.getVehiculo().getMarca(),
                        orden.getVehiculo().getModelo()),
                orden.getVehiculo().getUsuario().getEmail(),
                vuelta + "?pago=ok",
                vuelta + "?pago=cancelado"));

        presupuesto.setStripeSesionId(sesion.id());
        presupuestoRepository.save(presupuesto);
        return new SesionPagoResponse(sesion.url());
    }

    // Stripe reintenta los avisos: confirmar el mismo pago dos veces no debe tener efecto
    @Transactional
    public void confirmar(PagoCompletado pago) {
        Presupuesto presupuesto = presupuestoRepository.findByIdForUpdate(pago.presupuestoId()).orElse(null);
        if (presupuesto == null) {
            log.warn("Pago {} de un presupuesto inexistente ({})", pago.sesionId(), pago.presupuestoId());
            return;
        }
        if (presupuesto.getPagadoEn() != null) {
            return;
        }
        long esperado = enCentimos(presupuesto.getTotal());
        if (pago.importeCentimos() != esperado || !MONEDA.equalsIgnoreCase(pago.moneda())) {
            log.error("Pago {} del presupuesto {} por un importe distinto: {} {} en lugar de {} {}",
                    pago.sesionId(), presupuesto.getId(), pago.importeCentimos(), pago.moneda(), esperado, MONEDA);
            return;
        }

        presupuesto.setPagadoEn(LocalDateTime.now());
        presupuesto.setStripeSesionId(pago.sesionId());
        presupuesto.setMetodoPago(MetodoPago.tarjeta);
        presupuesto.setEstadoPago(EstadoPago.pagado_tarjeta);
        presupuestoRepository.save(presupuesto);

        notificacionService.notificarAdmins(
                presupuesto.getOrden(),
                TipoNotificacion.presupuesto_respondido,
                "El cliente ha pagado el presupuesto con tarjeta · " + importe(presupuesto) + " €");
    }

    // El cliente se compromete a pagar en el taller al recoger el vehículo. Puede cambiar
    // después de opinión y pagar con tarjeta: el pago con Stripe sigue disponible.
    @Transactional
    public void elegirEfectivo(Integer presupuestoId) {
        Presupuesto presupuesto = obtenerDelCliente(presupuestoId);
        comprobarPendienteDePago(presupuesto);
        if (presupuesto.getEstadoPago() == EstadoPago.pendiente_efectivo) {
            return;
        }

        presupuesto.setMetodoPago(MetodoPago.efectivo);
        presupuesto.setEstadoPago(EstadoPago.pendiente_efectivo);
        presupuestoRepository.save(presupuesto);

        notificacionService.notificarAdmins(
                presupuesto.getOrden(),
                TipoNotificacion.presupuesto_respondido,
                "El cliente pagará en efectivo al recoger el vehículo · " + importe(presupuesto) + " €");
    }

    // El taller registra que ha cobrado en mano, haya elegido o no el cliente el efectivo antes
    @Transactional
    public void cobrarEfectivo(Integer presupuestoId) {
        Presupuesto presupuesto = presupuestoRepository.findByIdForUpdate(presupuestoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Presupuesto no encontrado"));
        comprobarPendienteDePago(presupuesto);

        presupuesto.setMetodoPago(MetodoPago.efectivo);
        presupuesto.setEstadoPago(EstadoPago.pagado_efectivo);
        presupuesto.setPagadoEn(LocalDateTime.now());
        presupuestoRepository.save(presupuesto);

        notificacionService.notificarCliente(
                presupuesto.getOrden(),
                TipoNotificacion.presupuesto_respondido,
                "Hemos registrado tu pago en efectivo · " + importe(presupuesto) + " €");
    }

    private static void comprobarPendienteDePago(Presupuesto presupuesto) {
        if (presupuesto.getPagadoEn() != null) {
            throw new ConflictoException("Este presupuesto ya está pagado");
        }
        if (presupuesto.getEstado() != EstadoPresupuesto.autorizado) {
            throw new TransicionNoPermitidaException("Solo se puede pagar un presupuesto autorizado");
        }
    }

    private static String importe(Presupuesto presupuesto) {
        return NumberFormat.getNumberInstance(Locale.forLanguageTag("es-ES")).format(presupuesto.getTotal());
    }

    private Presupuesto obtenerDelCliente(Integer id) {
        Presupuesto presupuesto = presupuestoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Presupuesto no encontrado"));
        if (!SecurityUtils.esCliente()
                || !presupuesto.getOrden().getVehiculo().getUsuario().getId().equals(SecurityUtils.idActual())) {
            throw new RecursoNoEncontradoException("Presupuesto no encontrado");
        }
        return presupuesto;
    }

    private static long enCentimos(BigDecimal importe) {
        return importe == null ? 0 : importe.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
    }
}
