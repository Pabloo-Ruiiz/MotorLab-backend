package com.motorlab.dto.presupuesto;

import jakarta.validation.constraints.Size;

public record RechazoRequest(
        @Size(max = 255, message = "El motivo no puede superar 255 caracteres")
        String motivo) {
}
