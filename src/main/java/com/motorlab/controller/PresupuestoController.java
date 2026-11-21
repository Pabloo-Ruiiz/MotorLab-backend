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

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.motorlab.dto.presupuesto.LineaCreadaResponse;
import com.motorlab.dto.presupuesto.LineaRequest;
import com.motorlab.dto.presupuesto.PresupuestoDefinitivoResponse;
import com.motorlab.dto.presupuesto.PresupuestoResponse;
import com.motorlab.dto.presupuesto.PresupuestoUpdateRequest;
import com.motorlab.dto.presupuesto.RechazoRequest;
import com.motorlab.dto.presupuesto.TotalesResponse;
import com.motorlab.service.PresupuestoDefinitivoService;
import com.motorlab.service.PresupuestoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@Validated
@RequestMapping("/api/v1")
@Tag(name = "Presupuestos", description = "Gestión de presupuestos y sus líneas")
public class PresupuestoController {

    private final PresupuestoService presupuestoService;
    private final PresupuestoDefinitivoService presupuestoDefinitivoService;

    public PresupuestoController(
            PresupuestoService presupuestoService, PresupuestoDefinitivoService presupuestoDefinitivoService) {
        this.presupuestoService = presupuestoService;
        this.presupuestoDefinitivoService = presupuestoDefinitivoService;
    }

    @GetMapping("/ordenes/{ordenId}/presupuesto-definitivo")
    @PreAuthorize("hasAnyRole('CLIENTE','MECANICO','ADMIN')")
    @Operation(summary = "Consultar el presupuesto definitivo de una reparación finalizada",
            description = "Existe desde que la orden pasa a 'listo'. Recoge los trabajos, las piezas y los importes "
                    + "de la reparación y no cambia aunque varíen después los precios del catálogo")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada o reparación sin finalizar")
    })
    public PresupuestoDefinitivoResponse obtenerDefinitivo(@PathVariable @Positive Integer ordenId) {
        return presupuestoDefinitivoService.consultar(ordenId);
    }

    @GetMapping(path = "/ordenes/{ordenId}/presupuesto-definitivo/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('CLIENTE','MECANICO','ADMIN')")
    @Operation(summary = "Descargar en PDF el presupuesto definitivo de una reparación finalizada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento PDF"),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Rol no autorizado"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada o reparación sin finalizar")
    })
    public ResponseEntity<byte[]> descargarDefinitivo(@PathVariable @Positive Integer ordenId) {
        PresupuestoDefinitivoService.DocumentoPdf documento = presupuestoDefinitivoService.descargar(ordenId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(documento.nombreArchivo()).build().toString())
                .body(documento.contenido());
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
    public ResponseEntity<PresupuestoResponse> crear(@PathVariable @Positive Integer ordenId) {
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
    public PresupuestoResponse obtenerPorOrden(@PathVariable @Positive Integer ordenId) {
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
            @PathVariable @Positive Integer id, @Valid @RequestBody PresupuestoUpdateRequest solicitud) {
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
            @PathVariable @Positive Integer id, @Valid @RequestBody LineaRequest solicitud) {
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
            @PathVariable @Positive Integer id,
            @PathVariable @Positive Integer lineaId,
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
            @PathVariable @Positive Integer id, @PathVariable @Positive Integer lineaId) {
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
    public PresupuestoResponse enviar(@PathVariable @Positive Integer id) {
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
    public PresupuestoResponse autorizar(@PathVariable @Positive Integer id) {
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
            @PathVariable @Positive Integer id, @Valid @RequestBody RechazoRequest solicitud) {
        return presupuestoService.rechazar(id, solicitud);
    }
}
