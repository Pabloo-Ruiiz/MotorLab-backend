package com.motorlab.service.pago;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.motorlab.exception.FirmaWebhookInvalidaException;
import com.motorlab.service.pago.PasarelaPago.PagoCompletado;
import com.stripe.net.Webhook;

class StripePasarelaPagoTest {

    private static final String SECRETO = "whsec_pruebas";
    private final StripePasarelaPago pasarela = new StripePasarelaPago("sk_test_x", SECRETO, false);

    @Test
    void unAvisoFirmadoDePagoCompletadoDevuelveElPago() throws Exception {
        String cuerpo = evento("checkout.session.completed", "paid");

        assertThat(pasarela.leerAviso(cuerpo, firmar(cuerpo, SECRETO)))
                .contains(new PagoCompletado("cs_test_1", 5, 27213, "eur"));
    }

    @Test
    void unaFirmaFalsaSeRechaza() throws Exception {
        String cuerpo = evento("checkout.session.completed", "paid");

        assertThatThrownBy(() -> pasarela.leerAviso(cuerpo, firmar(cuerpo, "whsec_de_otro")))
                .isInstanceOf(FirmaWebhookInvalidaException.class);
        assertThatThrownBy(() -> pasarela.leerAviso(cuerpo, null))
                .isInstanceOf(FirmaWebhookInvalidaException.class);
    }

    @Test
    void unCuerpoModificadoTrasFirmarSeRechaza() throws Exception {
        String cuerpo = evento("checkout.session.completed", "paid");
        String firma = firmar(cuerpo, SECRETO);

        assertThatThrownBy(() -> pasarela.leerAviso(cuerpo.replace("27213", "1"), firma))
                .isInstanceOf(FirmaWebhookInvalidaException.class);
    }

    @Test
    void otrosEventosYPagosPendientesSeIgnoran() throws Exception {
        String otro = evento("checkout.session.expired", "unpaid");
        String pendiente = evento("checkout.session.completed", "unpaid");

        assertThat(pasarela.leerAviso(otro, firmar(otro, SECRETO))).isEmpty();
        assertThat(pasarela.leerAviso(pendiente, firmar(pendiente, SECRETO))).isEmpty();
    }

    @Test
    void unaClaveRealDesactivaLosPagos() {
        assertThat(new StripePasarelaPago("sk_live_x", SECRETO, false).configurada()).isFalse();
        assertThat(new StripePasarelaPago("", SECRETO, false).configurada()).isFalse();
        assertThat(pasarela.configurada()).isTrue();
    }

    private static String firmar(String cuerpo, String secreto) throws Exception {
        long instante = Webhook.Util.getTimeNow();
        String firma = Webhook.Util.computeHmacSha256(secreto, instante + "." + cuerpo);
        return "t=" + instante + ",v1=" + firma;
    }

    private static String evento(String tipo, String estadoPago) {
        return """
                {"id":"evt_1","object":"event","type":"%s","api_version":"2020-08-27",
                 "data":{"object":{"id":"cs_test_1","object":"checkout.session","payment_status":"%s",
                 "amount_total":27213,"currency":"eur","client_reference_id":"5",
                 "metadata":{"presupuestoId":"5"}}}}
                """.formatted(tipo, estadoPago);
    }
}
