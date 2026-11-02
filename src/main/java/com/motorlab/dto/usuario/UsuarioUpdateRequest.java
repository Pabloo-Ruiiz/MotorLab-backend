package com.motorlab.dto.usuario;

import com.motorlab.entity.enums.Rol;

import jakarta.validation.constraints.AssertTrue;

public record UsuarioUpdateRequest(Rol rol, Boolean activo) {

    @AssertTrue(message = "Debe indicar al menos el rol o el estado activo")
    public boolean tieneCambios() {
        return rol != null || activo != null;
    }
}
