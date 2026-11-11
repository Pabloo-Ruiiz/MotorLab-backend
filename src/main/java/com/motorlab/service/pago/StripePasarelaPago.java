package com.motorlab.service.pago;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.motorlab.exception.FirmaWebhookInvalidaException;
import com.motorlab.exception.PagosNoDisponiblesException;
import com.stripe.StripeClient;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

@Component
public class StripePasarelaPago implements PasarelaPago {

    private static final Logger log = LoggerFactory.getLogger(StripePasarelaPago.class);

    private static final String MONEDA = "eur";
    // checkout.session.completed llega antes del cobro en métodos diferidos: solo cuenta si ya está pagado
    private static final Set<String> EVENTOS_DE_PAGO = Set.of(
            "checkout.session.completed", "checkout.session.async_payment_succeeded");

    private final String claveSecreta;
    private final String secretoWebhook;
    private final StripeClient cliente;

    public StripePasarelaPago(
            @Value("${app.stripe.clave-secreta:}") String claveSecreta,
            @Value("${app.stripe.secreto-webhook:}") String secretoWebhook,
            @Value("${app.stripe.permitir-claves-reales:false}") boolean permitirClavesReales) {
        boolean claveReal = claveSecreta.startsWith("sk_live_") || claveSecreta.startsWith("rk_live_");
        if (claveReal && !permitirClavesReales) {
            // Proyecto de pruebas: una clave real cobraría dinero de verdad
            log.error("Se ha configurado una clave real de Stripe y solo se admiten claves de prueba: pagos desactivados");
            claveSecreta = "";
        }
        this.claveSecreta = claveSecreta;
        this.secretoWebhook = secretoWebhook;
        this.cliente = claveSecreta.isBlank() ? null : new StripeClient(claveSecreta);
    }

    @Override
    public boolean configurada() {
        return !claveSecreta.isBlank() && !secretoWebhook.isBlank();
    }

    @Override
    public SesionPago crearSesion(NuevaSesionPago datos) {
        if (!configurada()) {
            throw new PagosNoDisponiblesException();
        }
        SessionCreateParams parametros = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(datos.urlExito())
                .setCancelUrl(datos.urlCancelacion())
                .setCustomerEmail(datos.emailCliente())
                .setClientReferenceId(String.valueOf(datos.presupuestoId()))
                .putMetadata("presupuestoId", String.valueOf(datos.presupuestoId()))
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(MONEDA)
                                .setUnitAmount(datos.importeCentimos())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(datos.concepto())
                                        .build())
                                .build())
                        .build())
                .build();
        try {
            Session sesion = cliente.checkout().sessions().create(parametros);
            return new SesionPago(sesion.getId(), sesion.getUrl());
        } catch (StripeException excepcion) {
            log.error("Stripe no ha podido crear la sesión de pago del presupuesto {}", datos.presupuestoId(), excepcion);
            throw new PagosNoDisponiblesException("No se ha podido iniciar el pago. Inténtalo de nuevo más tarde");
        }
    }

    @Override
    public Optional<PagoCompletado> leerAviso(String cuerpo, String firma) {
        if (secretoWebhook.isBlank() || firma == null || firma.isBlank()) {
            throw new FirmaWebhookInvalidaException();
        }
        Event evento;
        try {
            // Verifica la firma sobre el cuerpo tal cual llega y rechaza avisos de más de 5 minutos
            evento = Webhook.constructEvent(cuerpo, firma, secretoWebhook);
        } catch (SignatureVerificationException excepcion) {
            throw new FirmaWebhookInvalidaException();
        }

        if (!EVENTOS_DE_PAGO.contains(evento.getType())) {
            return Optional.empty();
        }
        if (!(objetoDe(evento) instanceof Session sesion) || !"paid".equals(sesion.getPaymentStatus())) {
            return Optional.empty();
        }

        String referencia = sesion.getMetadata() == null ? null : sesion.getMetadata().get("presupuestoId");
        if (referencia == null) {
            referencia = sesion.getClientReferenceId();
        }
        Integer presupuestoId;
        try {
            presupuestoId = Integer.valueOf(referencia);
        } catch (NumberFormatException | NullPointerException excepcion) {
            log.warn("Pago de Stripe {} sin presupuesto asociado: se ignora", sesion.getId());
            return Optional.empty();
        }
        long importe = sesion.getAmountTotal() == null ? -1 : sesion.getAmountTotal();
        return Optional.of(new PagoCompletado(sesion.getId(), presupuestoId, importe, sesion.getCurrency()));
    }

    // Si la versión de la API del evento no coincide con la de la librería, se lee igualmente
    private StripeObject objetoDe(Event evento) {
        var deserializador = evento.getDataObjectDeserializer();
        return deserializador.getObject().orElseGet(() -> {
            try {
                return deserializador.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException excepcion) {
                log.warn("No se ha podido leer el evento de Stripe {}", evento.getId());
                return null;
            }
        });
    }
}
