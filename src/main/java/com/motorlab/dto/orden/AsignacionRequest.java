package com.motorlab.dto.orden;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AsignacionRequest(
        @NotNull(message = "El mecánico es obligatorio")
        Integer mecanicoId,
        @Size(max = 20, message = "El elevador no puede superar los 20 caracteres")
        String elevador,
        LocalDateTime fechaEstimada) {
}
