package com.motorlab.dto.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonAlias;

public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 150, message = "El email no puede superar los 150 caracteres")
        String email,
        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[0-9 +]{9,20}$", message = "El teléfono solo puede contener dígitos, espacios y el signo +")
        String telefono,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password,
        @JsonAlias("aceptaPrivacidad")
        @AssertTrue(message = "Debe aceptar la política de privacidad")
        boolean aceptaPrivacidad) {
}
