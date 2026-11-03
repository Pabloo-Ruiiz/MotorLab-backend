package com.motorlab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motorlab.dto.presupuesto.LineaCreadaResponse;
import com.motorlab.dto.presupuesto.LineaRequest;
import com.motorlab.dto.presupuesto.PresupuestoResponse;
import com.motorlab.dto.presupuesto.PresupuestoUpdateRequest;
import com.motorlab.dto.presupuesto.RechazoRequest;
import com.motorlab.dto.presupuesto.TotalesResponse;
import com.motorlab.service.PresupuestoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/v1")
@Tag(name = "Presupuestos", description = "Gestión de presupuestos y sus líneas")
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    public PresupuestoController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }

    @PostMapping("/ordenes/{ordenId}/presupuesto")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un presupuesto para una orden")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
            @ApiResponse(responseCode = "409", description = "La orden ya tiene presupuesto o no admite presupuesto")
    })
    public ResponseEntity<PresupuestoResponse> crear(@PathVariable Integer ordenId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presupuestoService.crear(ordenId));
    }

    @GetMapping("/ordenes/{ordenId}/presupuesto")
    @PreAuthorize("hasAnyRole('CLIENTE','MECANICO','ADMIN')")
    @Operation(summary = "Consultar el presupuesto de una orden")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "404", description = "Orden o presupuesto no encontrado")
    })
    public PresupuestoResponse obtenerPorOrden(@PathVariable Integer ordenId) {
        return presupuestoService.obtenerPorOrden(ordenId);
    }

    @PatchMapping("/presupuestos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un presupuesto")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado"),
            @ApiResponse(responseCode = "409", description = "El presupuesto no es editable")
    })
    public PresupuestoResponse actualizar(
            @PathVariable Integer id, @Valid @RequestBody PresupuestoUpdateRequest solicitud) {
        return presupuestoService.actualizar(id, solicitud);
    }

    @PostMapping("/presupuestos/{id}/lineas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Añadir una línea al presupuesto")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Presupuesto o pieza no encontrada"),
            @ApiResponse(responseCode = "409", description = "El presupuesto no es editable")
    })
    public ResponseEntity<LineaCreadaResponse> anadirLinea(
            @PathVariable Integer id, @Valid @RequestBody LineaRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(presupuestoService.anadirLinea(id, solicitud));
    }

    @PutMapping("/presupuestos/{id}/lineas/{lineaId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modificar una línea del presupuesto")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Presupuesto o línea no encontrada"),
            @ApiResponse(responseCode = "409", description = "El presupuesto no es editable")
    })
    public LineaCreadaResponse modificarLinea(
            @PathVariable Integer id,
            @PathVariable Integer lineaId,
            @Valid @RequestBody LineaRequest solicitud) {
        return presupuestoService.modificarLinea(id, lineaId, solicitud);
    }

    @DeleteMapping("/presupuestos/{id}/lineas/{lineaId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar una línea del presupuesto")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Presupuesto o línea no encontrada"),
            @ApiResponse(responseCode = "409", description = "El presupuesto no es editable")
    })
    public TotalesResponse eliminarLinea(
            @PathVariable Integer id, @PathVariable Integer lineaId) {
        return presupuestoService.eliminarLinea(id, lineaId);
    }

    @PostMapping("/presupuestos/{id}/enviar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enviar un presupuesto al cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo administradores"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado"),
            @ApiResponse(responseCode = "409", description = "El presupuesto no se puede enviar")
    })
    public PresupuestoResponse enviar(@PathVariable Integer id) {
        return presupuestoService.enviar(id);
    }

    @PostMapping("/presupuestos/{id}/autorizar")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Autorizar un presupuesto")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo el cliente propietario"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado"),
            @ApiResponse(responseCode = "409", description = "El presupuesto no está enviado")
    })
    public PresupuestoResponse autorizar(@PathVariable Integer id) {
        return presupuestoService.autorizar(id);
    }

    @PostMapping("/presupuestos/{id}/rechazar")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Rechazar un presupuesto")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo el cliente propietario"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado"),
            @ApiResponse(responseCode = "409", description = "El presupuesto no está enviado")
    })
    public PresupuestoResponse rechazar(
            @PathVariable Integer id, @Valid @RequestBody RechazoRequest solicitud) {
        return presupuestoService.rechazar(id, solicitud);
    }
}
