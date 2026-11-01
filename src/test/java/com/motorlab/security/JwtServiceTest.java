package com.motorlab.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.motorlab.config.JwtProperties;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.Rol;

class JwtServiceTest {

    private static final String SECRETO = "secreto-de-pruebas-con-mas-de-32-caracteres";

    @Test
    void tokenGeneradoEsValidoYDevuelveElId() {
        JwtService servicio = new JwtService(new JwtProperties(SECRETO, 8));
        Usuario usuario = usuario();

        String token = servicio.generarToken(UsuarioPrincipal.desde(usuario));

        assertTrue(servicio.esValido(token));
        assertEquals(42, servicio.extraerUsuarioId(token));
    }

    @Test
    void tokenManipuladoNoEsValido() {
        JwtService servicio = new JwtService(new JwtProperties(SECRETO, 8));
        String token = servicio.generarToken(UsuarioPrincipal.desde(usuario()));

        assertFalse(servicio.esValido(token + "manipulado"));
    }

    @Test
    void tokenCaducadoNoEsValido() {
        JwtService servicio = new JwtService(new JwtProperties(SECRETO, -1));
        String token = servicio.generarToken(UsuarioPrincipal.desde(usuario()));

        assertFalse(servicio.esValido(token));
    }

    @Test
    void secretoDemasiadoCortoLanzaExcepcion() {
        assertThrows(
                IllegalStateException.class,
                () -> new JwtService(new JwtProperties("secreto-corto", 8)));
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(42);
        usuario.setNombre("Usuario de prueba");
        usuario.setEmail("pruebas@example.com");
        usuario.setPasswordHash("hash");
        usuario.setRol(Rol.cliente);
        usuario.setActivo(true);
        return usuario;
    }
}
