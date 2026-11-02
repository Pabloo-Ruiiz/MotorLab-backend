package com.motorlab.dto.orden;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.motorlab.dto.presupuesto.PresupuestoResumenResponse;
import com.motorlab.dto.usuario.UsuarioResumenResponse;
import com.motorlab.dto.vehiculo.VehiculoResponse;
import com.motorlab.entity.enums.ConduccionSegura;
import com.motorlab.entity.enums.ContactoPreferido;
import com.motorlab.entity.enums.CuandoOcurre;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.FranjaPreferida;
import com.motorlab.entity.enums.TipoServicio;
import com.motorlab.entity.enums.Urgencia;

public record OrdenDetalleResponse(
        Integer id,
        EstadoOrden estado,
        int progreso,
        Urgencia urgencia,
        TipoServicio tipoServicio,
        String sintoma,
        CuandoOcurre cuandoOcurre,
        ConduccionSegura conduccionSegura,
        LocalDate fechaPreferida,
        FranjaPreferida franjaPreferida,
        ContactoPreferido contactoPreferido,
        String elevador,
        LocalDateTime fechaEntrada,
        LocalDateTime fechaEstimada,
        VehiculoResponse vehiculo,
        UsuarioResumenResponse cliente,
        UsuarioResumenResponse mecanico,
        PresupuestoResumenResponse presupuesto) {
}
