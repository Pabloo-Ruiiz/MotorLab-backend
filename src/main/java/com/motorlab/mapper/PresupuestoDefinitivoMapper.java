package com.motorlab.mapper;

import org.springframework.stereotype.Component;

import com.motorlab.config.DatosTaller;
import com.motorlab.dto.presupuesto.PresupuestoDefinitivoResponse;
import com.motorlab.entity.LineaPresupuestoDefinitivo;
import com.motorlab.entity.PresupuestoDefinitivo;

@Component
public class PresupuestoDefinitivoMapper {

    private final DatosTaller taller;

    public PresupuestoDefinitivoMapper(DatosTaller taller) {
        this.taller = taller;
    }

    public PresupuestoDefinitivoResponse aResponse(PresupuestoDefinitivo definitivo) {
        return new PresupuestoDefinitivoResponse(
                definitivo.getId(),
                definitivo.getNumero(),
                definitivo.getOrden().getId(),
                definitivo.getPresupuesto().getId(),
                definitivo.getEmitidoEn(),
                definitivo.getTipoServicio(),
                new PresupuestoDefinitivoResponse.Cliente(
                        definitivo.getClienteNombre(),
                        definitivo.getClienteEmail(),
                        definitivo.getClienteTelefono()),
                new PresupuestoDefinitivoResponse.Vehiculo(
                        definitivo.getVehiculoMatricula(),
                        definitivo.getVehiculoMarca(),
                        definitivo.getVehiculoModelo(),
                        definitivo.getVehiculoVersion(),
                        definitivo.getVehiculoAnio(),
                        definitivo.getVehiculoKilometros()),
                new PresupuestoDefinitivoResponse.Taller(
                        taller.nombre(), taller.direccion(), taller.telefono(), taller.email(), taller.cif()),
                definitivo.getLineas().stream().map(this::aLinea).toList(),
                definitivo.getSubtotalManoObra(),
                definitivo.getSubtotalPiezas(),
                definitivo.getSubtotal(),
                definitivo.getIvaPorcentaje(),
                definitivo.getIva(),
                definitivo.getTotal());
    }

    private PresupuestoDefinitivoResponse.Linea aLinea(LineaPresupuestoDefinitivo linea) {
        return new PresupuestoDefinitivoResponse.Linea(
                linea.getPosicion(),
                linea.getTipo(),
                linea.getReferencia(),
                linea.getConcepto(),
                linea.getCantidad(),
                linea.getPrecioUnitario(),
                linea.getImporte());
    }
}
