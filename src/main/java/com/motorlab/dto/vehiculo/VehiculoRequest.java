package com.motorlab.dto.vehiculo;

import com.motorlab.validation.MatriculaEspanola;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record VehiculoRequest(
        @NotBlank(message = "La matrícula es obligatoria")
        @MatriculaEspanola
        String matricula,
        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 50, message = "La marca no puede superar los 50 caracteres")
        String marca,
        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 80, message = "El modelo no puede superar los 80 caracteres")
        String modelo,
        @Size(max = 80, message = "La versión no puede superar los 80 caracteres")
        String version,
        @NotNull(message = "El año es obligatorio")
        @Min(value = 1980, message = "El año no puede ser anterior a 1980")
        Integer anio,
        @NotNull(message = "Los kilómetros son obligatorios")
        @PositiveOrZero(message = "Los kilómetros no pueden ser negativos")
        Integer kilometros,
        Integer usuarioId) {
}
