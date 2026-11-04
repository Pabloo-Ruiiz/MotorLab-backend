package com.motorlab.security;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorlab.dto.common.ErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LimiteIntentosLoginFilter extends OncePerRequestFilter {

    private static final int MAXIMO_INTENTOS = 5;
    private static final long DURACION_VENTANA_MILLIS = 60_000L;
    private static final String RUTA_LOGIN = "/api/v1/auth/login";

    private final ConcurrentHashMap<String, VentanaIntentos> intentosPorIp = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public LimiteIntentosLoginFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest peticion,
            HttpServletResponse respuesta,
            FilterChain cadena) throws ServletException, IOException {
        if (!esLogin(peticion)) {
            cadena.doFilter(peticion, respuesta);
            return;
        }

        String ip = obtenerIp(peticion);
        long ahora = System.currentTimeMillis();
        VentanaIntentos ventana = intentosPorIp.compute(ip, (clave, anterior) -> {
            if (anterior == null || ahora - anterior.inicioMillis >= DURACION_VENTANA_MILLIS) {
                return new VentanaIntentos(ahora, 1);
            }
            return new VentanaIntentos(anterior.inicioMillis, anterior.intentos + 1);
        });

        if (ventana.intentos > MAXIMO_INTENTOS) {
            responderDemasiadosIntentos(respuesta);
            return;
        }

        cadena.doFilter(peticion, respuesta);
    }

    private boolean esLogin(HttpServletRequest peticion) {
        return "POST".equalsIgnoreCase(peticion.getMethod())
                && RUTA_LOGIN.equals(peticion.getRequestURI());
    }

    private String obtenerIp(HttpServletRequest peticion) {
        return peticion.getRemoteAddr();
    }

    private void responderDemasiadosIntentos(HttpServletResponse respuesta) throws IOException {
        respuesta.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        ErrorResponse error = ErrorResponse.de(
                "DEMASIADOS_INTENTOS",
                "Demasiados intentos de inicio de sesión. Inténtelo de nuevo más tarde.",
                List.of());
        objectMapper.writeValue(respuesta.getWriter(), error);
    }

    private record VentanaIntentos(long inicioMillis, int intentos) {
    }
}
