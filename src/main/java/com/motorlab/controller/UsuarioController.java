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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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
    public PageResponse<UsuarioResponse> listar(
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer limite) {
        return usuarioService.listar(rol, pagina, limite);
    }

    @PostMapping
    @Operation(summary = "Crear un usuario")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(solicitud));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar rol o estado de un usuario")
    public UsuarioResponse actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioUpdateRequest solicitud) {
        return usuarioService.actualizar(id, solicitud);
    }
}
