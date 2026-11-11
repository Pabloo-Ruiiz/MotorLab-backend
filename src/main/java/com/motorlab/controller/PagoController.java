package com.motorlab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.motorlab.dto.pago.SesionPagoResponse;
import com.motorlab.service.PagoService;
import com.motorlab.service.pago.PasarelaPago;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;

@RestController
@Validated
@RequestMapping("/api/v1")
@Tag(name = "Pagos", description = "Pago de los presupuestos con tarjeta (Stripe) o en efectivo en el taller")
public class PagoController {

    private final PagoService pagoService;
    private final PasarelaPago pasarela;

    public PagoController(PagoService pagoService, PasarelaPago pasarela) {
        this.pagoService = pagoService;
        this.pasarela = pasarela;
    }

    @PostMapping("/presupuestos/{id}/pago")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Iniciar el pago de un presupuesto autorizado",
            description = "Devuelve la URL de la página de pago de Stripe a la que hay que redirigir al cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo clientes"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado"),
            @ApiResponse(responseCode = "409", description = "El presupuesto no está autorizado o ya está pagado"),
            @ApiResponse(responseCode = "503", description = "Los pagos no están configurados")
    })
    public SesionPagoResponse iniciar(@PathVariable @Positive Integer id) {
        return pagoService.iniciar(id);
    }

    @PostMapping("/presupuestos/{id}/pago/efectivo")
    @PreAuthorize("hasRole('CLIENTE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elegir pagar en efectivo al recoger el vehículo",
            description = "Registra el compromiso de pago en el taller. El presupuesto queda pendiente de cobro "
                    + "y el cliente aún puede pagar con tarjeta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pago en efectivo registrado"),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo clientes"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado"),
            @ApiResponse(responseCode = "409", description = "El presupuesto no está autorizado o ya está pagado")
    })
    public void elegirEfectivo(@PathVariable @Positive Integer id) {
        pagoService.elegirEfectivo(id);
    }

    @PostMapping("/presupuestos/{id}/cobro-efectivo")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Marcar un presupuesto como cobrado en efectivo",
            description = "Lo usa el taller cuando recibe el importe en mano, antes de entregar el vehículo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cobro registrado"),
            @ApiResponse(responseCode = "401", description = "Autenticación requerida"),
            @ApiResponse(responseCode = "403", description = "Solo administración"),
            @ApiResponse(responseCode = "404", description = "Presupuesto no encontrado"),
            @ApiResponse(responseCode = "409", description = "El presupuesto no está autorizado o ya está pagado")
    })
    public void cobrarEfectivo(@PathVariable @Positive Integer id) {
        pagoService.cobrarEfectivo(id);
    }

    // Público: lo llama Stripe y se autentica con la firma, no con un JWT
    @PostMapping(path = "/pagos/stripe/webhook", consumes = MediaType.ALL_VALUE)
    @Operation(summary = "Aviso de Stripe sobre un pago (uso interno)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aviso recibido"),
            @ApiResponse(responseCode = "400", description = "Firma no válida")
    })
    public ResponseEntity<Void> avisoStripe(
            @RequestBody String cuerpo,
            @RequestHeader(name = "Stripe-Signature", required = false) String firma) {
        pasarela.leerAviso(cuerpo, firma).ifPresent(pagoService::confirmar);
        return ResponseEntity.ok().build();
    }
}
