package com.motorlab.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

@ExtendWith(OutputCaptureExtension.class)
class CorreoVerificacionServiceTest {

    private static final String ENLACE = "http://localhost:4200/verificar-correo?token=abc";

    @SuppressWarnings("unchecked")
    private final CorreoVerificacionService servicio = new CorreoVerificacionService(
            mock(ObjectProvider.class),
            mock(GmailApiClient.class),
            "",
            "taller@motorlab.example",
            "http://localhost:4200",
            false);

    @Test
    void elEnlaceApuntaALaPantallaDelFrontend() {
        assertThat(servicio.enlaceDe("abc-123_x"))
                .isEqualTo("http://localhost:4200/verificar-correo?token=abc-123_x");
    }

    @Test
    void laPlantillaIncluyeElEnlaceYLosColoresDeLaWeb() {
        String html = servicio.html("Laura", "http://localhost:4200/verificar-correo?token=abc");

        assertThat(html)
                .contains("Hola, Laura:")
                .contains("href=\"http://localhost:4200/verificar-correo?token=abc\"")
                .contains("#f2ede5", "#0a0a0a")
                .doesNotContain("{{");
    }

    @Test
    void elNombreSeEscapaParaNoInyectarHtml() {
        String html = servicio.html("<img src=x>", "http://localhost:4200/x");

        assertThat(html).doesNotContain("<img src=x>").contains("&lt;img src=x&gt;");
    }

    @Test
    void siFallaElEnvioElEnlaceSaleEnElLogCuandoEstaActivado(CapturedOutput salida) {
        servicioConCorreoCaido(true).enviar(solicitud());

        assertThat(salida).contains("Correo no enviado", ENLACE);
    }

    @Test
    void siFallaElEnvioElEnlaceNoSaleEnElLogPorDefecto(CapturedOutput salida) {
        servicioConCorreoCaido(false).enviar(solicitud());

        assertThat(salida).contains("No se ha podido enviar").doesNotContain(ENLACE);
    }

    @Test
    @SuppressWarnings("unchecked")
    void conLaApiDeGmailConfiguradaNoSeUsaSmtp() throws Exception {
        GmailApiClient gmail = mock(GmailApiClient.class);
        when(gmail.configurado()).thenReturn(true);
        ObjectProvider<JavaMailSender> proveedor = mock(ObjectProvider.class);
        CorreoVerificacionService conGmail = new CorreoVerificacionService(
                proveedor, gmail, "smtp.example", "taller@motorlab.example", "http://localhost:4200", false);

        conGmail.enviar(solicitud());

        verify(gmail).enviar(any(MimeMessage.class));
        verifyNoInteractions(proveedor);
    }

    @SuppressWarnings("unchecked")
    private CorreoVerificacionService servicioConCorreoCaido(boolean enlaceEnLog) {
        JavaMailSender correo = mock(JavaMailSender.class);
        when(correo.createMimeMessage()).thenThrow(new MailSendException("sin conexión"));
        ObjectProvider<JavaMailSender> proveedor = mock(ObjectProvider.class);
        when(proveedor.getIfAvailable()).thenReturn(correo);
        return new CorreoVerificacionService(
                proveedor,
                mock(GmailApiClient.class),
                "smtp.example",
                "taller@motorlab.example",
                "http://localhost:4200",
                enlaceEnLog);
    }

    private CorreoVerificacionSolicitado solicitud() {
        return new CorreoVerificacionSolicitado("laura@cliente.example", "Laura", "abc");
    }
}
