package com.motorlab.mapper;

import org.springframework.stereotype.Component;

import com.motorlab.dto.vehiculo.VehiculoResponse;
import com.motorlab.entity.Vehiculo;

@Component
public class VehiculoMapper {

    public VehiculoResponse aResponse(Vehiculo vehiculo) {
        return new VehiculoResponse(
                vehiculo.getId(),
                vehiculo.getUsuario().getId(),
                vehiculo.getMatricula(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getVersion(),
                vehiculo.getAnio(),
                vehiculo.getKilometros(),
                vehiculo.isActivo());
    }
}
