package com.motorlab.dto.orden;

import com.motorlab.entity.enums.EstadoOrden;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambioEstadoRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoOrden estado,
        @Size(max = 200, message = "La descripción no puede superar los 200 caracteres")
        String descripcion) {
}
