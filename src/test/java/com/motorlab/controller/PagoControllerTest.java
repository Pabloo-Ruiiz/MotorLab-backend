package com.motorlab.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.motorlab.support.IntegracionBase;

@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-caracteres",
        "app.stripe.clave-secreta=sk_test_pruebas",
        "app.stripe.secreto-webhook=whsec_pruebas"
})
class PagoControllerTest extends IntegracionBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void elWebhookEsPublicoPeroExigeLaFirmaDeStripe() throws Exception {
        mockMvc.perform(post("/api/v1/pagos/stripe/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"checkout.session.completed\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.codigo").value("FIRMA_NO_VALIDA"));

        mockMvc.perform(post("/api/v1/pagos/stripe/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "t=1,v1=firma-falsa")
                        .content("{\"type\":\"checkout.session.completed\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void iniciarUnPagoExigeIniciarSesion() throws Exception {
        mockMvc.perform(post("/api/v1/presupuestos/1/pago"))
                .andExpect(status().isUnauthorized());
    }
}
