package com.motorlab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.motorlab.dto.auth.LoginRequest;
import com.motorlab.dto.auth.LoginResponse;
import com.motorlab.dto.auth.ReenviarVerificacionRequest;
import com.motorlab.dto.auth.RegistroRequest;
import com.motorlab.dto.auth.VerificarEmailRequest;
import com.motorlab.dto.usuario.UsuarioResponse;
import com.motorlab.service.AuthService;
import com.motorlab.service.VerificacionEmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión y usuario autenticado")
public class AuthController {

    private final AuthService authService;
    private final VerificacionEmailService verificacionEmailService;

    public AuthController(AuthService authService, VerificacionEmailService verificacionEmailService) {
        this.authService = authService;
        this.verificacionEmailService = verificacionEmailService;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar un cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(solicitud));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas"),
            @ApiResponse(responseCode = "403", description = "El correo aún no está confirmado")
    })
    public LoginResponse login(@Valid @RequestBody LoginRequest solicitud) {
        return authService.login(solicitud);
    }

    @PostMapping("/verificar-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Confirmar el correo con el token recibido")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Correo confirmado"),
            @ApiResponse(responseCode = "400", description = "Token no válido o caducado")
    })
    public void verificarEmail(@Valid @RequestBody VerificarEmailRequest solicitud) {
        verificacionEmailService.verificar(solicitud.token());
    }

    @PostMapping("/reenviar-verificacion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Volver a enviar el correo de confirmación")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Solicitud atendida, exista o no la cuenta")
    })
    public void reenviarVerificacion(@Valid @RequestBody ReenviarVerificacionRequest solicitud) {
        verificacionEmailService.reenviar(solicitud.email());
    }

    @GetMapping("/yo")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consultar el usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida")
    })
    public UsuarioResponse yo() {
        return authService.yo();
    }
}
