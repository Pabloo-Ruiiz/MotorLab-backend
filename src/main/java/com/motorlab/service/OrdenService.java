package com.motorlab.service;

import jakarta.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.dto.common.PageResponse;
import com.motorlab.dto.orden.OrdenCreateRequest;
import com.motorlab.dto.orden.OrdenCreateResponse;
import com.motorlab.dto.orden.OrdenDetalleResponse;
import com.motorlab.dto.orden.OrdenResumenResponse;
import com.motorlab.dto.orden.AsignacionRequest;
import com.motorlab.dto.orden.CambioEstadoRequest;
import com.motorlab.entity.OrdenResumen;
import com.motorlab.entity.OrdenTrabajo;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.Vehiculo;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.EstadoPresupuesto;
import com.motorlab.entity.enums.Rol;
import com.motorlab.entity.enums.TipoNotificacion;
import com.motorlab.exception.RecursoNoEncontradoException;
import com.motorlab.exception.SolicitudInvalidaException;
import com.motorlab.exception.SinPermisoException;
import com.motorlab.exception.TransicionNoPermitidaException;
import com.motorlab.mapper.OrdenMapper;
import com.motorlab.repository.OrdenResumenSpecifications;
import com.motorlab.repository.OrdenResumenRepository;
import com.motorlab.repository.OrdenTrabajoRepository;
import com.motorlab.repository.PresupuestoRepository;
import com.motorlab.repository.UsuarioRepository;
import com.motorlab.repository.VehiculoRepository;
import com.motorlab.security.SecurityUtils;

@Service
public class OrdenService {

    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final OrdenResumenRepository ordenResumenRepository;
    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final OrdenMapper ordenMapper;
    private final SeguimientoService seguimientoService;
    private final NotificacionService notificacionService;
    private final OrdenEstadoService ordenEstadoService;
    private final EntityManager entityManager;

    public OrdenService(
            OrdenTrabajoRepository ordenTrabajoRepository,
            OrdenResumenRepository ordenResumenRepository,
            VehiculoRepository vehiculoRepository,
            UsuarioRepository usuarioRepository,
            PresupuestoRepository presupuestoRepository,
            OrdenMapper ordenMapper,
            SeguimientoService seguimientoService,
            NotificacionService notificacionService,
            OrdenEstadoService ordenEstadoService,
            EntityManager entityManager) {
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.ordenResumenRepository = ordenResumenRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.ordenMapper = ordenMapper;
        this.seguimientoService = seguimientoService;
        this.notificacionService = notificacionService;
        this.ordenEstadoService = ordenEstadoService;
        this.entityManager = entityManager;
    }

    @Transactional
    public OrdenCreateResponse crear(OrdenCreateRequest solicitud) {
        Vehiculo vehiculo = vehiculoRepository.findById(solicitud.vehiculoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado"));
        if (!SecurityUtils.esAdmin() && !vehiculo.getUsuario().getId().equals(SecurityUtils.idActual())) {
            throw new RecursoNoEncontradoException("Vehículo no encontrado");
        }

        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setVehiculo(vehiculo);
        orden.setTipoServicio(solicitud.tipoServicio());
        orden.setSintoma(solicitud.sintoma().trim());
        orden.setCuandoOcurre(solicitud.cuandoOcurre());
        orden.setConduccionSegura(solicitud.conduccionSegura());
        orden.setUrgencia(solicitud.urgencia());
        orden.setFechaPreferida(solicitud.fechaPreferida());
        orden.setFranjaPreferida(solicitud.franjaPreferida());
        orden.setContactoPreferido(solicitud.contactoPreferido());
        orden.setEstado(EstadoOrden.solicitud_recibida);

        ordenTrabajoRepository.save(orden);
        ordenTrabajoRepository.flush();
        entityManager.refresh(orden);

        Usuario autor = usuarioRepository.findById(SecurityUtils.idActual())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        seguimientoService.registrar(orden, orden.getEstado(), "Solicitud recibida", autor);
        notificacionService.notificarAdmins(
                orden, TipoNotificacion.solicitud_nueva, "Se ha recibido una nueva solicitud");

        return new OrdenCreateResponse(
                orden.getId(),
                orden.getEstado(),
                orden.getUrgencia(),
                "Solicitud recibida. El taller la revisará en breve.");
    }

    @Transactional(readOnly = true)
    public PageResponse<OrdenResumenResponse> listar(
            EstadoOrden estado,
            com.motorlab.entity.enums.Urgencia urgencia,
            Integer mecanicoId,
            String q,
            Integer pagina,
            Integer limite) {
        Specification<OrdenResumen> especificacion = null;
        especificacion = combinar(especificacion, OrdenResumenSpecifications.conEstado(estado));
        especificacion = combinar(especificacion, OrdenResumenSpecifications.conUrgencia(urgencia));
        especificacion = combinar(especificacion, OrdenResumenSpecifications.buscar(q));

        if (SecurityUtils.esCliente()) {
            especificacion = combinar(especificacion, OrdenResumenSpecifications.delCliente(SecurityUtils.idActual()));
        } else if (SecurityUtils.esMecanico()) {
            especificacion = combinar(especificacion, OrdenResumenSpecifications.delMecanico(SecurityUtils.idActual()));
        } else if (mecanicoId != null) {
            especificacion = combinar(especificacion, OrdenResumenSpecifications.conMecanico(mecanicoId));
        }

        Pageable pageable = PageResponse.pageable(pagina, limite);
        Page<OrdenResumen> ordenes = ordenResumenRepository.findAll(especificacion, pageable);
        return PageResponse.de(ordenes.map(ordenMapper::aResumenResponse));
    }

    @Transactional(readOnly = true)
    public OrdenDetalleResponse obtener(Integer id) {
        OrdenTrabajo orden = obtenerEntidadAutorizada(id);
        return ordenMapper.aDetalleResponse(
                orden, presupuestoRepository.findByOrdenId(id).orElse(null));
    }

    @Transactional
    public OrdenDetalleResponse asignar(Integer id, AsignacionRequest solicitud) {
        if (!SecurityUtils.esAdmin()) {
            throw new SolicitudInvalidaException("Solo un administrador puede asignar órdenes");
        }
        OrdenTrabajo orden = ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        Usuario mecanico = usuarioRepository.findById(solicitud.mecanicoId())
                .filter(usuario -> usuario.isActivo() && usuario.getRol() == Rol.mecanico)
                .orElseThrow(() -> new SolicitudInvalidaException(
                        "El usuario indicado no es un mecánico activo"));
        orden.setMecanico(mecanico);
        orden.setElevador(solicitud.elevador() == null ? null : solicitud.elevador().trim());
        orden.setFechaEstimada(solicitud.fechaEstimada());
        return ordenMapper.aDetalleResponse(
                ordenTrabajoRepository.save(orden),
                presupuestoRepository.findByOrdenId(id).orElse(null));
    }

    @Transactional
    public OrdenDetalleResponse cambiarEstado(Integer id, CambioEstadoRequest solicitud) {
        OrdenTrabajo orden;
        try {
            orden = obtenerEntidadAutorizada(id);
        } catch (RecursoNoEncontradoException excepcion) {
            if (SecurityUtils.esMecanico() && ordenTrabajoRepository.existsById(id)) {
                throw new SinPermisoException("El mecánico no tiene asignada esta orden");
            }
            throw excepcion;
        }

        if (SecurityUtils.esMecanico()
                && (orden.getMecanico() == null
                        || !orden.getMecanico().getId().equals(SecurityUtils.idActual()))) {
            throw new SinPermisoException("El mecánico no tiene asignada esta orden");
        }
        if (SecurityUtils.esAdmin()
                && TransicionesEstado.ESTADOS_AUTOMATICOS.contains(solicitud.estado())) {
            throw new TransicionNoPermitidaException(
                    "El estado " + solicitud.estado() + " se aplica automáticamente");
        }
        if (SecurityUtils.esMecanico()
                && !TransicionesEstado.ESTADOS_MECANICO.contains(solicitud.estado())) {
            throw new SinPermisoException("El mecánico no puede aplicar ese estado");
        }

        Usuario autor = usuarioRepository.findById(SecurityUtils.idActual())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        OrdenTrabajo actualizada = ordenEstadoService.aplicarCambio(
                orden, solicitud.estado(), solicitud.descripcion(), autor);
        return ordenMapper.aDetalleResponse(
                actualizada, presupuestoRepository.findByOrdenId(id).orElse(null));
    }

    @Transactional
    public OrdenDetalleResponse cancelar(Integer id) {
        OrdenTrabajo orden = ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        boolean administrador = SecurityUtils.esAdmin();
        boolean propietario = orden.getVehiculo().getUsuario().getId().equals(SecurityUtils.idActual());
        if (!administrador && !SecurityUtils.esCliente()) {
            throw new SinPermisoException("Solo el cliente propietario o un administrador puede cancelar");
        }
        if (!administrador && !propietario) {
            throw new SinPermisoException("Solo el cliente propietario puede cancelar la orden");
        }
        boolean estadoCancelable = orden.getEstado() == EstadoOrden.solicitud_recibida
                || orden.getEstado() == EstadoOrden.pendiente_presupuesto
                || orden.getEstado() == EstadoOrden.presupuesto_enviado
                || (administrador && orden.getEstado() == EstadoOrden.presupuesto_autorizado);
        if (!estadoCancelable) {
            throw new TransicionNoPermitidaException(
                    "La orden no se puede cancelar en estado " + orden.getEstado());
        }

        presupuestoRepository.findByOrdenId(id).ifPresent(presupuesto -> {
            if (presupuesto.getEstado() == EstadoPresupuesto.borrador
                    || presupuesto.getEstado() == EstadoPresupuesto.enviado) {
                presupuesto.setEstado(EstadoPresupuesto.rechazado);
                presupuesto.setMotivoRechazo("Orden cancelada");
                presupuestoRepository.save(presupuesto);
            }
        });

        Usuario autor = usuarioRepository.findById(SecurityUtils.idActual())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        OrdenTrabajo cancelada = ordenEstadoService.aplicarCambio(
                orden, EstadoOrden.cancelada, "Orden cancelada", autor);
        return ordenMapper.aDetalleResponse(
                cancelada, presupuestoRepository.findByOrdenId(id).orElse(null));
    }

    @Transactional(readOnly = true)
    public OrdenTrabajo obtenerEntidadAutorizada(Integer id) {
        OrdenTrabajo orden = ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        boolean propietario = orden.getVehiculo().getUsuario().getId().equals(SecurityUtils.idActual());
        boolean mecanicoAsignado = orden.getMecanico() != null
                && orden.getMecanico().getId().equals(SecurityUtils.idActual());
        if (!SecurityUtils.esAdmin() && !propietario && !mecanicoAsignado) {
            throw new RecursoNoEncontradoException("Orden no encontrada");
        }
        return orden;
    }

    private Specification<OrdenResumen> combinar(
            Specification<OrdenResumen> actual,
            Specification<OrdenResumen> nueva) {
        return nueva == null ? actual : actual == null ? nueva : actual.and(nueva);
    }
}
