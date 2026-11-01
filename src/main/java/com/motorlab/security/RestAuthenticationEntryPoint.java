package com.motorlab.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorlab.dto.common.ErrorResponse;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest peticion,
            HttpServletResponse respuesta,
            AuthenticationException excepcion) throws IOException, ServletException {
        respuesta.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                respuesta.getOutputStream(),
                ErrorResponse.de(
                        "NO_AUTENTICADO",
                        "Autenticación requerida o token caducado",
                        List.of()));
    }
}
