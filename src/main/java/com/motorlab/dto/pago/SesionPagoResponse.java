package com.motorlab.dto.pago;

// Página de pago alojada por Stripe a la que el frontend redirige al cliente
public record SesionPagoResponse(String url) {
}
