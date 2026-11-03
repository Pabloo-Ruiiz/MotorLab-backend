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
import io.swagger.v3.oas.annotations.tags.Tag;

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
    public PageResponse<NotificacionResponse> listar(
            @RequestParam(required = false) Boolean leida,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer limite) {
        return notificacionService.listar(leida, pagina, limite);
    }

    @PatchMapping("/{id}/leida")
    @Operation(summary = "Marcar una notificación como leída")
    public NotificacionResponse marcarLeida(@PathVariable Integer id) {
        return notificacionService.marcarLeida(id);
    }

    @PostMapping("/marcar-todas")
    @Operation(summary = "Marcar todas las notificaciones como leídas")
    public Map<String, Integer> marcarTodas() {
        return Map.of("marcadas", notificacionService.marcarTodas());
    }

    @GetMapping("/contador")
    @Operation(summary = "Contar notificaciones no leídas")
    public Map<String, Long> contador() {
        return Map.of("no_leidas", notificacionService.contarNoLeidas());
    }
}
