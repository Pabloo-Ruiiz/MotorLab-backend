package com.motorlab.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motorlab.dto.presupuesto.AvisoStockResponse;
import com.motorlab.dto.presupuesto.LineaResponse;
import com.motorlab.dto.presupuesto.PresupuestoResponse;
import com.motorlab.dto.presupuesto.TotalesResponse;
import com.motorlab.entity.LineaPresupuesto;
import com.motorlab.entity.Presupuesto;

@Component
public class PresupuestoMapper {

    public LineaResponse aLineaResponse(LineaPresupuesto linea) {
        return new LineaResponse(
                linea.getId(),
                linea.getTipo(),
                linea.getPieza() == null ? null : linea.getPieza().getId(),
                linea.getPieza() == null ? null : linea.getPieza().getReferencia(),
                linea.getConcepto(),
                linea.getCantidad(),
                linea.getPrecioUnitario(),
                linea.getImporte());
    }

    public TotalesResponse aTotalesResponse(Presupuesto presupuesto) {
        return new TotalesResponse(
                presupuesto.getSubtotal(),
                presupuesto.getIvaPorcentaje(),
                presupuesto.getIva(),
                presupuesto.getTotal());
    }

    public PresupuestoResponse aResponse(
            Presupuesto presupuesto,
            List<LineaPresupuesto> lineas,
            List<AvisoStockResponse> avisos) {
        return new PresupuestoResponse(
                presupuesto.getId(),
                presupuesto.getOrden().getId(),
                presupuesto.getEstado(),
                lineas.stream().map(this::aLineaResponse).toList(),
                presupuesto.getSubtotal(),
                presupuesto.getIvaPorcentaje(),
                presupuesto.getIva(),
                presupuesto.getTotal(),
                presupuesto.getTiempoEstimadoHoras(),
                presupuesto.getGarantiaMeses(),
                presupuesto.getMotivoRechazo(),
                presupuesto.getEnviadoEn(),
                presupuesto.getRespondidoEn(),
                avisos);
    }
}
