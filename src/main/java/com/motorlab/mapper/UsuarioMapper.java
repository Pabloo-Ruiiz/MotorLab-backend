package com.motorlab.mapper;

import org.springframework.stereotype.Component;

import com.motorlab.dto.usuario.UsuarioResumenResponse;
import com.motorlab.dto.usuario.UsuarioResponse;
import com.motorlab.entity.Usuario;

@Component
public class UsuarioMapper {

    public UsuarioResponse aResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getRol(),
                usuario.isActivo());
    }

    public UsuarioResumenResponse aResumen(Usuario usuario) {
        return new UsuarioResumenResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getTelefono());
    }
}
