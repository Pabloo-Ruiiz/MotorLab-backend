package com.motorlab.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.entity.Usuario;
import com.motorlab.exception.TokenVerificacionInvalidoException;
import com.motorlab.repository.UsuarioRepository;

@Service
public class VerificacionEmailService {

    static final Duration VIGENCIA_TOKEN = Duration.ofHours(24);
    static final Duration ESPERA_ENTRE_ENVIOS = Duration.ofSeconds(60);

    private static final int BYTES_TOKEN = 32;

    private final SecureRandom aleatorio = new SecureRandom();
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventos;

    public VerificacionEmailService(UsuarioRepository usuarioRepository, ApplicationEventPublisher eventos) {
        this.usuarioRepository = usuarioRepository;
        this.eventos = eventos;
    }

    // En la base de datos solo se guarda el hash: quien la lea no puede confirmar cuentas ajenas
    void emitirToken(Usuario usuario) {
        byte[] bytes = new byte[BYTES_TOKEN];
        aleatorio.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        usuario.setTokenVerificacionHash(hash(token));
        usuario.setTokenVerificacionExpira(LocalDateTime.now().plus(VIGENCIA_TOKEN));
        eventos.publishEvent(new CorreoVerificacionSolicitado(usuario.getEmail(), usuario.getNombre(), token));
    }

    @Transactional
    public void verificar(String token) {
        Usuario usuario = usuarioRepository.findByTokenVerificacionHash(hash(token))
                .filter(candidato -> candidato.getTokenVerificacionExpira().isAfter(LocalDateTime.now()))
                .orElseThrow(TokenVerificacionInvalidoException::new);

        usuario.setEmailVerificado(true);
        usuario.setTokenVerificacionHash(null);
        usuario.setTokenVerificacionExpira(null);
    }

    // No informa del resultado para no revelar qué correos están registrados
    @Transactional
    public void reenviar(String email) {
        usuarioRepository.findByEmail(AuthService.normalizarEmail(email))
                .filter(usuario -> usuario.isActivo() && !usuario.isEmailVerificado())
                .filter(this::puedeRecibirOtroCorreo)
                .ifPresent(this::emitirToken);
    }

    private boolean puedeRecibirOtroCorreo(Usuario usuario) {
        LocalDateTime expira = usuario.getTokenVerificacionExpira();
        return expira == null
                || expira.minus(VIGENCIA_TOKEN).plus(ESPERA_ENTRE_ENVIOS).isBefore(LocalDateTime.now());
    }

    static String hash(String token) {
        try {
            byte[] resumen = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException excepcion) {
            throw new IllegalStateException("SHA-256 no disponible", excepcion);
        }
    }
}
