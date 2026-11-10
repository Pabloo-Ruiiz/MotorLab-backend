package com.motorlab.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import com.motorlab.entity.Usuario;
import com.motorlab.exception.TokenVerificacionInvalidoException;
import com.motorlab.repository.UsuarioRepository;

class VerificacionEmailServiceTest {

    private final UsuarioRepository repositorio = mock(UsuarioRepository.class);
    private final ApplicationEventPublisher eventos = mock(ApplicationEventPublisher.class);
    private final VerificacionEmailService servicio = new VerificacionEmailService(repositorio, eventos);

    private Usuario sinVerificar() {
        Usuario usuario = new Usuario();
        usuario.setEmail("laura@cliente.example");
        usuario.setNombre("Laura");
        usuario.setEmailVerificado(false);
        return usuario;
    }

    @Test
    void emitirGuardaSoloElHashYEnviaElToken() {
        Usuario usuario = sinVerificar();

        servicio.emitirToken(usuario);

        ArgumentCaptor<CorreoVerificacionSolicitado> evento =
                ArgumentCaptor.forClass(CorreoVerificacionSolicitado.class);
        verify(eventos).publishEvent(evento.capture());
        String token = evento.getValue().token();
        assertThat(usuario.getTokenVerificacionHash())
                .isNotEqualTo(token)
                .isEqualTo(VerificacionEmailService.hash(token));
        assertThat(usuario.getTokenVerificacionExpira()).isAfter(LocalDateTime.now().plusHours(23));
    }

    @Test
    void verificarConfirmaLaCuentaYConsumeElToken() {
        Usuario usuario = sinVerificar();
        usuario.setTokenVerificacionHash(VerificacionEmailService.hash("abc"));
        usuario.setTokenVerificacionExpira(LocalDateTime.now().plusHours(1));
        when(repositorio.findByTokenVerificacionHash(VerificacionEmailService.hash("abc")))
                .thenReturn(Optional.of(usuario));

        servicio.verificar("abc");

        assertThat(usuario.isEmailVerificado()).isTrue();
        assertThat(usuario.getTokenVerificacionHash()).isNull();
        assertThat(usuario.getTokenVerificacionExpira()).isNull();
    }

    @Test
    void unTokenCaducadoNoVerifica() {
        Usuario usuario = sinVerificar();
        usuario.setTokenVerificacionExpira(LocalDateTime.now().minusMinutes(1));
        when(repositorio.findByTokenVerificacionHash(any())).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> servicio.verificar("abc"))
                .isInstanceOf(TokenVerificacionInvalidoException.class);
        assertThat(usuario.isEmailVerificado()).isFalse();
    }

    @Test
    void unTokenDesconocidoNoVerifica() {
        when(repositorio.findByTokenVerificacionHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.verificar("abc"))
                .isInstanceOf(TokenVerificacionInvalidoException.class);
    }

    @Test
    void reenviarRespetaLaEsperaEntreEnvios() {
        Usuario usuario = sinVerificar();
        usuario.setTokenVerificacionExpira(
                LocalDateTime.now().plus(VerificacionEmailService.VIGENCIA_TOKEN).minusSeconds(5));
        when(repositorio.findByEmail("laura@cliente.example")).thenReturn(Optional.of(usuario));

        servicio.reenviar(" Laura@Cliente.example ");

        verify(eventos, never()).publishEvent(any(Object.class));
    }

    @Test
    void reenviarEmiteOtroTokenPasadaLaEspera() {
        Usuario usuario = sinVerificar();
        usuario.setTokenVerificacionExpira(LocalDateTime.now().plusHours(1));
        when(repositorio.findByEmail("laura@cliente.example")).thenReturn(Optional.of(usuario));

        servicio.reenviar("laura@cliente.example");

        verify(eventos).publishEvent(any(Object.class));
    }

    @Test
    void reenviarNoHaceNadaSiYaEstaVerificado() {
        Usuario usuario = sinVerificar();
        usuario.setEmailVerificado(true);
        when(repositorio.findByEmail("laura@cliente.example")).thenReturn(Optional.of(usuario));

        servicio.reenviar("laura@cliente.example");

        verify(eventos, never()).publishEvent(any(Object.class));
    }
}
