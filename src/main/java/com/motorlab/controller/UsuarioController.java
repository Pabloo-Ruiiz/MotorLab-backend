package com.motorlab.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motorlab.dto.common.PageResponse;
import com.motorlab.dto.usuario.UsuarioCreateRequest;
import com.motorlab.dto.usuario.UsuarioResponse;
import com.motorlab.dto.usuario.UsuarioUpdateRequest;
import com.motorlab.entity.enums.Rol;
import com.motorlab.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@RestController
@Validated
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios por administración")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo administradores")
    })
    public PageResponse<UsuarioResponse> listar(
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) @Positive Integer pagina,
            @RequestParam(required = false) @Min(1) @Max(100) Integer limite) {
        return usuarioService.listar(rol, pagina, limite);
    }

    @PostMapping
    @Operation(summary = "Crear un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo administradores"),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(solicitud));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar rol o estado de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "El cambio entra en conflicto con los datos existentes")
    })
    public UsuarioResponse actualizar(
            @PathVariable @Positive Integer id,
            @Valid @RequestBody UsuarioUpdateRequest solicitud) {
        return usuarioService.actualizar(id, solicitud);
    }
}
