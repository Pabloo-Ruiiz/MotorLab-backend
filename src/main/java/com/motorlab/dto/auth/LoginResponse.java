package com.motorlab.dto.auth;

import com.motorlab.dto.usuario.UsuarioResponse;

public record LoginResponse(String token, UsuarioResponse usuario) {
}
