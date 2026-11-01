package com.motorlab.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UsuarioPrincipal usuarioActual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null
                || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken
                || !(autenticacion.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new IllegalStateException("No hay un usuario autenticado");
        }
        return principal;
    }

    public static Integer idActual() {
        return usuarioActual().getId();
    }

    public static boolean esAdmin() {
        return usuarioActual().getRol().name().equals("admin");
    }

    public static boolean esMecanico() {
        return usuarioActual().getRol().name().equals("mecanico");
    }

    public static boolean esCliente() {
        return usuarioActual().getRol().name().equals("cliente");
    }
}
