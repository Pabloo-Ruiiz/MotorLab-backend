package com.motorlab.service.pago;

import java.util.Optional;

// Separa la lógica de negocio de la librería de Stripe y permite probarla sin red
public interface PasarelaPago {

    boolean configurada();

    SesionPago crearSesion(NuevaSesionPago datos);

    /**
     * Verifica la firma del aviso y devuelve el pago que describe.
     * Vacío si el evento no es de un pago completado.
     *
     * @throws com.motorlab.exception.FirmaWebhookInvalidaException si la firma no es válida
     */
    Optional<PagoCompletado> leerAviso(String cuerpo, String firma);

    record NuevaSesionPago(
            Integer presupuestoId,
            long importeCentimos,
            String concepto,
            String emailCliente,
            String urlExito,
            String urlCancelacion) {
    }

    record SesionPago(String id, String url) {
    }

    record PagoCompletado(String sesionId, Integer presupuestoId, long importeCentimos, String moneda) {
    }
}
