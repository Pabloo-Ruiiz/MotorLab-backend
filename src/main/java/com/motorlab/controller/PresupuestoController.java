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
import com.motorlab.dto.presupuesto.TotalesResponse;
import com.motorlab.service.PresupuestoService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/api/v1")
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    public PresupuestoController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }

    @PostMapping("/ordenes/{ordenId}/presupuesto")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PresupuestoResponse> crear(@PathVariable Integer ordenId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presupuestoService.crear(ordenId));
    }

    @GetMapping("/ordenes/{ordenId}/presupuesto")
    @PreAuthorize("hasAnyRole('CLIENTE','MECANICO','ADMIN')")
    public PresupuestoResponse obtenerPorOrden(@PathVariable Integer ordenId) {
        return presupuestoService.obtenerPorOrden(ordenId);
    }

    @PatchMapping("/presupuestos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PresupuestoResponse actualizar(
            @PathVariable Integer id, @Valid @RequestBody PresupuestoUpdateRequest solicitud) {
        return presupuestoService.actualizar(id, solicitud);
    }

    @PostMapping("/presupuestos/{id}/lineas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LineaCreadaResponse> anadirLinea(
            @PathVariable Integer id, @Valid @RequestBody LineaRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(presupuestoService.anadirLinea(id, solicitud));
    }

    @PutMapping("/presupuestos/{id}/lineas/{lineaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public LineaCreadaResponse modificarLinea(
            @PathVariable Integer id,
            @PathVariable Integer lineaId,
            @Valid @RequestBody LineaRequest solicitud) {
        return presupuestoService.modificarLinea(id, lineaId, solicitud);
    }

    @DeleteMapping("/presupuestos/{id}/lineas/{lineaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public TotalesResponse eliminarLinea(
            @PathVariable Integer id, @PathVariable Integer lineaId) {
        return presupuestoService.eliminarLinea(id, lineaId);
    }
}
