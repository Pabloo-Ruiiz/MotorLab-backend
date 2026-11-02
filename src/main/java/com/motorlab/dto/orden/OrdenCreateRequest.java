package com.motorlab.dto.orden;

import java.time.LocalDate;

import com.motorlab.entity.enums.ConduccionSegura;
import com.motorlab.entity.enums.ContactoPreferido;
import com.motorlab.entity.enums.CuandoOcurre;
import com.motorlab.entity.enums.FranjaPreferida;
import com.motorlab.entity.enums.TipoServicio;
import com.motorlab.entity.enums.Urgencia;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrdenCreateRequest(
        @NotNull(message = "El vehículo es obligatorio")
        Integer vehiculoId,
        @NotNull(message = "El tipo de servicio es obligatorio")
        TipoServicio tipoServicio,
        @NotBlank(message = "El síntoma es obligatorio")
        @Size(min = 20, max = 2000, message = "El síntoma debe tener entre 20 y 2000 caracteres")
        String sintoma,
        CuandoOcurre cuandoOcurre,
        @NotNull(message = "La conducción segura es obligatoria")
        ConduccionSegura conduccionSegura,
        @NotNull(message = "La urgencia es obligatoria")
        Urgencia urgencia,
        @NotNull(message = "La fecha preferida es obligatoria")
        @FutureOrPresent(message = "La fecha preferida debe ser hoy o posterior")
        LocalDate fechaPreferida,
        @NotNull(message = "La franja preferida es obligatoria")
        FranjaPreferida franjaPreferida,
        @NotNull(message = "El contacto preferido es obligatorio")
        ContactoPreferido contactoPreferido,
        @AssertTrue(message = "Debe aceptar la política de privacidad")
        boolean aceptaPrivacidad) {
}
