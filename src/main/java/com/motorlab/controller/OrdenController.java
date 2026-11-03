package com.motorlab.controller;

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
import com.motorlab.dto.orden.AsignacionRequest;
import com.motorlab.dto.orden.CambioEstadoRequest;
import com.motorlab.dto.orden.OrdenCreateRequest;
import com.motorlab.dto.orden.OrdenCreateResponse;
import com.motorlab.dto.orden.OrdenDetalleResponse;
import com.motorlab.dto.orden.OrdenResumenResponse;
import com.motorlab.dto.orden.SeguimientoResponse;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.Urgencia;
import com.motorlab.service.OrdenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@PreAuthorize("hasAnyRole('CLIENTE','MECANICO','ADMIN')")
@RequestMapping("/api/v1/ordenes")
@Tag(name = "Órdenes de trabajo", description = "Gestión de solicitudes y órdenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    @Operation(summary = "Crear una solicitud de reparación")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<OrdenCreateResponse> crear(@Valid @RequestBody OrdenCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenService.crear(solicitud));
    }

    @PatchMapping("/{id}/asignacion")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Asignar un mecánico a una orden")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public OrdenDetalleResponse asignar(
            @PathVariable Integer id,
            @Valid @RequestBody AsignacionRequest solicitud) {
        return ordenService.asignar(id, solicitud);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','MECANICO')")
    @Operation(summary = "Cambiar el estado de una orden")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado o mecánico no asignado"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "409", description = "Transición no permitida o stock insuficiente")
    })
    public OrdenDetalleResponse cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody CambioEstadoRequest solicitud) {
        return ordenService.cambiarEstado(id, solicitud);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    @Operation(summary = "Cancelar una orden")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "El usuario no puede cancelar esta orden"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "409", description = "La orden no se puede cancelar en su estado actual")
    })
    public OrdenDetalleResponse cancelar(@PathVariable Integer id) {
        return ordenService.cancelar(id);
    }

    @GetMapping
    @Operation(summary = "Listar órdenes")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado")
    })
    public PageResponse<OrdenResumenResponse> listar(
            @RequestParam(required = false) EstadoOrden estado,
            @RequestParam(required = false) Urgencia urgencia,
            @RequestParam(required = false) Integer mecanicoId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer limite) {
        return ordenService.listar(estado, urgencia, mecanicoId, q, pagina, limite);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle de una orden")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public OrdenDetalleResponse obtener(@PathVariable Integer id) {
        return ordenService.obtener(id);
    }

    @GetMapping("/{id}/seguimiento")
    @Operation(summary = "Consultar el seguimiento de una orden")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public SeguimientoResponse seguimiento(@PathVariable Integer id) {
        return ordenService.seguimiento(id);
    }
}
