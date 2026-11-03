package com.motorlab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motorlab.dto.common.PageResponse;
import com.motorlab.dto.orden.OrdenResumenResponse;
import com.motorlab.dto.vehiculo.VehiculoRequest;
import com.motorlab.dto.vehiculo.VehiculoResponse;
import com.motorlab.service.VehiculoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
@RequestMapping("/api/v1/vehiculos")
@Tag(name = "Vehículos", description = "Gestión de vehículos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    @Operation(summary = "Listar vehículos")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado")
    })
    public PageResponse<VehiculoResponse> listar(
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer limite) {
        return vehiculoService.listar(pagina, limite);
    }

    @PostMapping
    @Operation(summary = "Crear un vehículo")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "409", description = "La matrícula ya está registrada")
    })
    public ResponseEntity<VehiculoResponse> crear(@Valid @RequestBody VehiculoRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crear(solicitud));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un vehículo")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public VehiculoResponse obtener(@PathVariable Integer id) {
        return vehiculoService.obtener(id);
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Consultar el historial de un vehículo")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public PageResponse<OrdenResumenResponse> historial(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer limite) {
        return vehiculoService.historial(id, pagina, limite);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un vehículo")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado"),
            @ApiResponse(responseCode = "409", description = "La matrícula ya está registrada")
    })
    public VehiculoResponse actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody VehiculoRequest solicitud) {
        return vehiculoService.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Dar de baja un vehículo")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado"),
            @ApiResponse(responseCode = "409", description = "El vehículo tiene órdenes abiertas")
    })
    public ResponseEntity<Void> darDeBaja(@PathVariable Integer id) {
        vehiculoService.darDeBaja(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
