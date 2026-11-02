package com.motorlab.dto.vehiculo;

public record VehiculoResponse(
        Integer id,
        Integer usuarioId,
        String matricula,
        String marca,
        String modelo,
        String version,
        Integer anio,
        Integer kilometros,
        boolean activo) {
}
