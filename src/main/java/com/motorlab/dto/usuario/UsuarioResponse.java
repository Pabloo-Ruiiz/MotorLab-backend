package com.motorlab.dto.usuario;

import com.motorlab.entity.enums.Rol;

public record UsuarioResponse(
        Integer id,
        String nombre,
        String email,
        String telefono,
        Rol rol,
        boolean activo) {
}
