package com.motorlab.controller;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motorlab.dto.common.PageResponse;
import com.motorlab.dto.notificacion.NotificacionResponse;
import com.motorlab.service.NotificacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@RestController
@Validated
@PreAuthorize("hasAnyRole('CLIENTE','MECANICO','ADMIN')")
@RequestMapping("/api/v1/notificaciones")
@Tag(name = "Notificaciones", description = "Avisos del usuario autenticado")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    @Operation(summary = "Listar notificaciones")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado")
    })
    public PageResponse<NotificacionResponse> listar(
            @RequestParam(required = false) Boolean leida,
            @RequestParam(required = false) @Positive Integer pagina,
            @RequestParam(required = false) @Min(1) @Max(100) Integer limite) {
        return notificacionService.listar(leida, pagina, limite);
    }

    @PatchMapping("/{id}/leida")
    @Operation(summary = "Marcar una notificación como leída")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    public NotificacionResponse marcarLeida(@PathVariable @Positive Integer id) {
        return notificacionService.marcarLeida(id);
    }

    @PostMapping("/marcar-todas")
    @Operation(summary = "Marcar todas las notificaciones como leídas")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado")
    })
    public Map<String, Integer> marcarTodas() {
        return Map.of("marcadas", notificacionService.marcarTodas());
    }

    @GetMapping("/contador")
    @Operation(summary = "Contar notificaciones no leídas")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado")
    })
    public Map<String, Long> contador() {
        return Map.of("no_leidas", notificacionService.contarNoLeidas());
    }
}
