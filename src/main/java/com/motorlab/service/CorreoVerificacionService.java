package com.motorlab.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class CorreoVerificacionService {

    private static final Logger log = LoggerFactory.getLogger(CorreoVerificacionService.class);

    private static final String ASUNTO = "Confirma tu correo · Taller MotorLab";
    private static final String NOMBRE_REMITENTE = "Taller MotorLab";

    private final ObjectProvider<JavaMailSender> proveedorCorreo;
    private final GmailApiClient gmail;
    private final String hostCorreo;
    private final String remitente;
    private final String urlFrontend;
    private final boolean enlaceEnLog;

    public CorreoVerificacionService(
            ObjectProvider<JavaMailSender> proveedorCorreo,
            GmailApiClient gmail,
            @Value("${spring.mail.host:}") String hostCorreo,
            @Value("${app.correo.remitente}") String remitente,
            @Value("${app.frontend-url}") String urlFrontend,
            @Value("${app.correo.enlace-en-log:false}") boolean enlaceEnLog) {
        this.proveedorCorreo = proveedorCorreo;
        this.gmail = gmail;
        this.hostCorreo = hostCorreo;
        this.remitente = remitente;
        this.urlFrontend = urlFrontend;
        this.enlaceEnLog = enlaceEnLog;
    }

    // Un fallo del servidor de correo no debe deshacer el registro: el cliente puede pedir otro envío
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviar(CorreoVerificacionSolicitado solicitud) {
        String enlace = enlaceDe(solicitud.token());
        boolean porApiGmail = gmail.configurado();
        // Con la API de Gmail, JavaMailSenderImpl solo construye el mensaje: no abre ninguna conexión SMTP
        JavaMailSender correo = porApiGmail
                ? new JavaMailSenderImpl()
                : hostCorreo.isBlank() ? null : proveedorCorreo.getIfAvailable();

        if (correo == null) {
            log.warn("SMTP sin configurar. Enlace de verificación para {}: {}", solicitud.email(), enlace);
            return;
        }

        try {
            MimeMessage mensaje = correo.createMimeMessage();
            MimeMessageHelper ayudante = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());
            ayudante.setFrom(remitente, NOMBRE_REMITENTE);
            ayudante.setTo(solicitud.email());
            ayudante.setSubject(ASUNTO);
            ayudante.setText(textoPlano(solicitud.nombre(), enlace), html(solicitud.nombre(), enlace));
            ayudante.addInline("logo", new ClassPathResource("correo/logo.png"), "image/png");
            if (porApiGmail) {
                gmail.enviar(mensaje);
            } else {
                correo.send(mensaje);
            }
        } catch (MailException | MessagingException | IOException | RestClientException excepcion) {
            log.error("No se ha podido enviar el correo de verificación a {}", solicitud.email(), excepcion);
            // Solo para desarrollo: el enlace da acceso a verificar la cuenta y no debe acabar en logs reales
            if (enlaceEnLog) {
                log.warn("Correo no enviado. Enlace de verificación para {}: {}", solicitud.email(), enlace);
            }
        }
    }

    String enlaceDe(String token) {
        return UriComponentsBuilder.fromUriString(urlFrontend)
                .path("/verificar-correo")
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    // El nombre lo escribe el usuario: sin escapar podría inyectar HTML en el correo
    String html(String nombre, String enlace) {
        return plantilla()
                .replace("{{nombre}}", HtmlUtils.htmlEscape(nombre))
                .replace("{{enlace}}", HtmlUtils.htmlEscape(enlace));
    }

    private String textoPlano(String nombre, String enlace) {
        return """
                Hola, %s:

                Confirma tu correo para empezar a seguir tus reparaciones en Taller MotorLab:
                %s

                El enlace caduca en 24 horas. Si no has creado esta cuenta, ignora este mensaje.
                """.formatted(nombre, enlace);
    }

    private String plantilla() {
        try {
            return new ClassPathResource("correo/verificacion.html").getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }
}
