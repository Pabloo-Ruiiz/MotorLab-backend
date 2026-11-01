package com.motorlab.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.motorlab.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final JwtProperties propiedades;
    private final SecretKey clave;

    public JwtService(JwtProperties propiedades) {
        this.propiedades = propiedades;
        if (propiedades.secret() == null || propiedades.secret().length() < 32) {
            throw new IllegalStateException("El secreto JWT debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(propiedades.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(UsuarioPrincipal usuario) {
        Date ahora = new Date();
        Date caducidad = new Date(ahora.getTime() + propiedades.expiracionHoras() * 60L * 60L * 1000L);

        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("rol", usuario.getRol().name())
                .claim("nombre", usuario.getNombre())
                .issuedAt(ahora)
                .expiration(caducidad)
                .signWith(clave)
                .compact();
    }

    public Integer extraerUsuarioId(String token) {
        Claims reclamaciones = Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Integer.valueOf(reclamaciones.getSubject());
    }

    public boolean esValido(String token) {
        try {
            extraerUsuarioId(token);
            return true;
        } catch (JwtException | IllegalArgumentException excepcion) {
            return false;
        }
    }
}
