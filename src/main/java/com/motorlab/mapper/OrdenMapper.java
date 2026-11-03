package com.motorlab.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.motorlab.dto.orden.OrdenDetalleResponse;
import com.motorlab.dto.orden.OrdenResumenResponse;
import com.motorlab.dto.orden.EventoResponse;
import com.motorlab.dto.orden.SeguimientoResponse;
import com.motorlab.dto.presupuesto.PresupuestoResumenResponse;
import com.motorlab.entity.EventoSeguimiento;
import com.motorlab.entity.OrdenResumen;
import com.motorlab.entity.OrdenTrabajo;
import com.motorlab.entity.Presupuesto;

@Component
public class OrdenMapper {

    private final UsuarioMapper usuarioMapper;
    private final VehiculoMapper vehiculoMapper;

    public OrdenMapper(UsuarioMapper usuarioMapper, VehiculoMapper vehiculoMapper) {
        this.usuarioMapper = usuarioMapper;
        this.vehiculoMapper = vehiculoMapper;
    }

    public OrdenResumenResponse aResumenResponse(OrdenResumen orden) {
        return new OrdenResumenResponse(
                orden.getOrdenId(),
                orden.getEstado(),
                orden.getProgreso(),
                orden.getUrgencia(),
                orden.getTipoServicio(),
                orden.getMatricula(),
                orden.getMarca(),
                orden.getModelo(),
                orden.getClienteNombre(),
                orden.getMecanicoNombre(),
                orden.getElevador(),
                orden.getFechaEntrada(),
                orden.getFechaEstimada(),
                orden.getPresupuestoEstado(),
                orden.getPresupuestoTotal());
    }

    public OrdenDetalleResponse aDetalleResponse(OrdenTrabajo orden, Presupuesto presupuesto) {
        return new OrdenDetalleResponse(
                orden.getId(),
                orden.getEstado(),
                orden.getEstado().progreso(),
                orden.getUrgencia(),
                orden.getTipoServicio(),
                orden.getSintoma(),
                orden.getCuandoOcurre(),
                orden.getConduccionSegura(),
                orden.getFechaPreferida(),
                orden.getFranjaPreferida(),
                orden.getContactoPreferido(),
                orden.getElevador(),
                orden.getFechaEntrada(),
                orden.getFechaEstimada(),
                vehiculoMapper.aResponse(orden.getVehiculo()),
                usuarioMapper.aResumen(orden.getVehiculo().getUsuario()),
                orden.getMecanico() == null ? null : usuarioMapper.aResumen(orden.getMecanico()),
                presupuesto == null ? null : new PresupuestoResumenResponse(
                        presupuesto.getId(), presupuesto.getEstado(), presupuesto.getTotal()));
    }

    public SeguimientoResponse aSeguimientoResponse(
            OrdenTrabajo orden, List<EventoSeguimiento> eventos) {
        String version = orden.getVehiculo().getVersion();
        String vehiculo = orden.getVehiculo().getMarca()
                + " " + orden.getVehiculo().getModelo()
                + (version == null || version.isBlank() ? "" : " " + version)
                + " · " + orden.getVehiculo().getMatricula();
        return new SeguimientoResponse(
                orden.getId(),
                vehiculo,
                orden.getEstado(),
                orden.getEstado().progreso(),
                orden.getFechaEstimada(),
                eventos.stream()
                        .map(evento -> new EventoResponse(
                                evento.getEstado(),
                                evento.getDescripcion(),
                                evento.getFechaHora()))
                        .toList());
    }
}
