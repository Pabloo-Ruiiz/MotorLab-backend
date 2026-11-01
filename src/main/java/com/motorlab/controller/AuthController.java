package com.motorlab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motorlab.dto.auth.LoginRequest;
import com.motorlab.dto.auth.LoginResponse;
import com.motorlab.dto.auth.RegistroRequest;
import com.motorlab.dto.usuario.UsuarioResponse;
import com.motorlab.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión y usuario autenticado")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar un cliente")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(solicitud));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public LoginResponse login(@Valid @RequestBody LoginRequest solicitud) {
        return authService.login(solicitud);
    }

    @GetMapping("/yo")
    @Operation(summary = "Consultar el usuario autenticado")
    public UsuarioResponse yo() {
        return authService.yo();
    }
}
