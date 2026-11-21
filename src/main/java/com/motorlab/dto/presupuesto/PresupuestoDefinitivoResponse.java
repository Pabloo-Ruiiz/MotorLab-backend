package com.motorlab.dto.presupuesto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.motorlab.entity.enums.TipoLinea;
import com.motorlab.entity.enums.TipoServicio;

// Presupuesto emitido al finalizar la reparación: los importes no cambian una vez emitido
public record PresupuestoDefinitivoResponse(
        Integer id,
        String numero,
        Integer ordenId,
        Integer presupuestoId,
        LocalDateTime emitidoEn,
        TipoServicio tipoServicio,
        Cliente cliente,
        Vehiculo vehiculo,
        Taller taller,
        List<Linea> lineas,
        BigDecimal subtotalManoObra,
        BigDecimal subtotalPiezas,
        BigDecimal subtotal,
        BigDecimal ivaPorcentaje,
        BigDecimal iva,
        BigDecimal total) {

    public record Cliente(String nombre, String email, String telefono) {
    }

    public record Vehiculo(
            String matricula, String marca, String modelo, String version, Integer anio, Integer kilometros) {
    }

    // Los datos sin configurar llegan como null
    public record Taller(String nombre, String direccion, String telefono, String email, String cif) {
    }

    public record Linea(
            Integer posicion,
            TipoLinea tipo,
            String referencia,
            String concepto,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            BigDecimal importe) {
    }
}
