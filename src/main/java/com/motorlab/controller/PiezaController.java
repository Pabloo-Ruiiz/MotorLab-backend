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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.motorlab.dto.common.PageResponse;
import com.motorlab.dto.pieza.PiezaRequest;
import com.motorlab.dto.pieza.PiezaResponse;
import com.motorlab.dto.pieza.StockDeltaRequest;
import com.motorlab.service.PiezaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@PreAuthorize("hasAnyRole('MECANICO','ADMIN')")
@RequestMapping("/api/v1/piezas")
@Tag(name = "Piezas", description = "Catálogo y stock de piezas")
public class PiezaController {

    private final PiezaService piezaService;

    public PiezaController(PiezaService piezaService) {
        this.piezaService = piezaService;
    }

    @GetMapping
    @Operation(summary = "Listar piezas")
    public PageResponse<PiezaResponse> listar(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "true") boolean soloActivas,
            @RequestParam(defaultValue = "false") boolean stockBajo,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer limite) {
        return piezaService.listar(q, soloActivas, stockBajo, pagina, limite);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una pieza")
    public PiezaResponse obtener(@PathVariable Integer id) {
        return piezaService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear una pieza")
    public ResponseEntity<PiezaResponse> crear(@Valid @RequestBody PiezaRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(piezaService.crear(solicitud));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar una pieza")
    public PiezaResponse actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody PiezaRequest solicitud) {
        return piezaService.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dar de baja una pieza")
    public ResponseEntity<Void> darDeBaja(@PathVariable Integer id) {
        piezaService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ajustar el stock de una pieza")
    public PiezaResponse ajustarStock(
            @PathVariable Integer id,
            @Valid @RequestBody StockDeltaRequest solicitud) {
        return piezaService.ajustarStock(id, solicitud.delta(), solicitud.motivo());
    }
}
