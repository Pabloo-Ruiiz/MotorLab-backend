package com.motorlab.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

// Envía por HTTPS: sirve en redes que bloquean los puertos de correo (SMTP)
@Component
public class GmailApiClient {

    private static final String URL_TOKEN = "https://oauth2.googleapis.com/token";
    private static final String URL_ENVIO = "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";
    private static final Duration TIEMPO_MAXIMO = Duration.ofSeconds(10);

    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final RestClient http;

    @Autowired
    public GmailApiClient(
            @Value("${app.correo.gmail.client-id:}") String clientId,
            @Value("${app.correo.gmail.client-secret:}") String clientSecret,
            @Value("${app.correo.gmail.refresh-token:}") String refreshToken) {
        this(clientId, clientSecret, refreshToken, RestClient.builder().requestFactory(fabricaConTiempoMaximo()));
    }

    GmailApiClient(String clientId, String clientSecret, String refreshToken, RestClient.Builder constructor) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.http = constructor.build();
    }

    public boolean configurado() {
        return !clientId.isBlank() && !clientSecret.isBlank() && !refreshToken.isBlank();
    }

    public void enviar(MimeMessage mensaje) throws MessagingException, IOException {
        ByteArrayOutputStream contenido = new ByteArrayOutputStream();
        mensaje.writeTo(contenido);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(contenido.toByteArray());

        http.post()
                .uri(URL_ENVIO)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDeAcceso())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("raw", raw))
                .retrieve()
                .toBodilessEntity();
    }

    private String tokenDeAcceso() {
        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("client_id", clientId);
        formulario.add("client_secret", clientSecret);
        formulario.add("refresh_token", refreshToken);
        formulario.add("grant_type", "refresh_token");

        Map<String, Object> respuesta = http.post()
                .uri(URL_TOKEN)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formulario)
                .retrieve()
                .body(new ParameterizedTypeReference<>() { });

        if (respuesta == null || !(respuesta.get("access_token") instanceof String token)) {
            throw new RestClientException("Google no ha devuelto un token de acceso");
        }
        return token;
    }

    private static JdkClientHttpRequestFactory fabricaConTiempoMaximo() {
        HttpClient cliente = HttpClient.newBuilder().connectTimeout(TIEMPO_MAXIMO).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(cliente);
        fabrica.setReadTimeout(TIEMPO_MAXIMO);
        return fabrica;
    }
}
