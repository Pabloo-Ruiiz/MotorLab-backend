package com.motorlab.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

class GmailApiClientTest {

    private static final String URL_TOKEN = "https://oauth2.googleapis.com/token";
    private static final String URL_ENVIO = "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";

    private final RestClient.Builder constructor = RestClient.builder();
    private final MockRestServiceServer google = MockRestServiceServer.bindTo(constructor).build();
    private final GmailApiClient cliente = new GmailApiClient("id", "secreto", "refresco", constructor);

    @Test
    void obtieneUnTokenYEnviaElMensajeCodificado() throws Exception {
        StringBuilder raw = new StringBuilder();
        google.expect(requestTo(URL_TOKEN))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(containsString("refresh_token=refresco")))
                .andExpect(content().string(containsString("grant_type=refresh_token")))
                .andRespond(withSuccess("{\"access_token\":\"acceso\"}", MediaType.APPLICATION_JSON));
        google.expect(requestTo(URL_ENVIO))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer acceso"))
                .andExpect(peticion -> raw.append(campoRaw((MockClientHttpRequest) peticion)))
                .andRespond(withSuccess("{\"id\":\"1\"}", MediaType.APPLICATION_JSON));

        cliente.enviar(mensaje());

        google.verify();
        String decodificado = new String(Base64.getUrlDecoder().decode(raw.toString()), StandardCharsets.UTF_8);
        assertThat(decodificado).contains("Subject: Prueba", "laura@cliente.example");
    }

    @Test
    void siGoogleRechazaLaAutorizacionFallaElEnvio() {
        google.expect(requestTo(URL_TOKEN)).andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> cliente.enviar(mensaje())).isInstanceOf(RestClientException.class);
    }

    @Test
    void soloEstaConfiguradoConLasTresCredenciales() {
        assertThat(cliente.configurado()).isTrue();
        assertThat(new GmailApiClient("id", "secreto", "", constructor).configurado()).isFalse();
    }

    private static String campoRaw(MockClientHttpRequest peticion) {
        Matcher coincidencia = Pattern.compile("\"raw\":\"([^\"]+)\"").matcher(peticion.getBodyAsString());
        assertThat(coincidencia.find()).isTrue();
        return coincidencia.group(1);
    }

    private static MimeMessage mensaje() throws Exception {
        MimeMessage mensaje = new MimeMessage((Session) null);
        mensaje.setFrom(new InternetAddress("taller@motorlab.example"));
        mensaje.setRecipients(MimeMessage.RecipientType.TO, "laura@cliente.example");
        mensaje.setSubject("Prueba");
        mensaje.setText("Hola");
        return mensaje;
    }
}
