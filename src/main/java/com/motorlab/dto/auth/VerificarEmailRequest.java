package com.motorlab.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerificarEmailRequest(
        @NotBlank(message = "El token es obligatorio")
        @Size(max = 100, message = "El token no es válido")
        String token) {
}
