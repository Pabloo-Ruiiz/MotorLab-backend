package com.motorlab.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.dto.common.ErrorDetalle;
import com.motorlab.dto.presupuesto.AvisoStockResponse;
import com.motorlab.dto.presupuesto.LineaCreadaResponse;
import com.motorlab.dto.presupuesto.LineaRequest;
import com.motorlab.dto.presupuesto.PresupuestoResponse;
import com.motorlab.dto.presupuesto.PresupuestoUpdateRequest;
import com.motorlab.dto.presupuesto.RechazoRequest;
import com.motorlab.dto.presupuesto.TotalesResponse;
import com.motorlab.entity.LineaPresupuesto;
import com.motorlab.entity.OrdenTrabajo;
import com.motorlab.entity.Pieza;
import com.motorlab.entity.Presupuesto;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.EstadoPresupuesto;
import com.motorlab.entity.enums.TipoLinea;
import com.motorlab.exception.PresupuestoNoEditableException;
import com.motorlab.exception.PresupuestoYaExisteException;
import com.motorlab.exception.RecursoNoEncontradoException;
import com.motorlab.exception.SolicitudInvalidaException;
import com.motorlab.exception.TransicionNoPermitidaException;
import com.motorlab.mapper.PresupuestoMapper;
import com.motorlab.repository.LineaPresupuestoRepository;
import com.motorlab.repository.OrdenTrabajoRepository;
import com.motorlab.repository.PiezaRepository;
import com.motorlab.repository.PresupuestoRepository;
import com.motorlab.repository.UsuarioRepository;
import com.motorlab.security.SecurityUtils;

@Service
public class PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;
    private final LineaPresupuestoRepository lineaRepository;
    private final OrdenTrabajoRepository ordenRepository;
    private final PiezaRepository piezaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PresupuestoMapper mapper;
    private final OrdenEstadoService ordenEstadoService;
    private final NotificacionService notificacionService;
    private final EntityManager entityManager;

    public PresupuestoService(
            PresupuestoRepository presupuestoRepository,
            LineaPresupuestoRepository lineaRepository,
            OrdenTrabajoRepository ordenRepository,
            PiezaRepository piezaRepository,
            UsuarioRepository usuarioRepository,
            PresupuestoMapper mapper,
            OrdenEstadoService ordenEstadoService,
            NotificacionService notificacionService,
            EntityManager entityManager) {
        this.presupuestoRepository = presupuestoRepository;
        this.lineaRepository = lineaRepository;
        this.ordenRepository = ordenRepository;
        this.piezaRepository = piezaRepository;
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
        this.ordenEstadoService = ordenEstadoService;
        this.notificacionService = notificacionService;
        this.entityManager = entityManager;
    }

    @Transactional
    public PresupuestoResponse crear(Integer ordenId) {
        exigirAdministrador();
        OrdenTrabajo orden = obtenerOrden(ordenId);
        if (presupuestoRepository.findByOrdenId(ordenId).isPresent()) {
            throw new PresupuestoYaExisteException();
        }
        if (orden.getEstado() != EstadoOrden.solicitud_recibida
                && orden.getEstado() != EstadoOrden.pendiente_presupuesto) {
            throw new com.motorlab.exception.TransicionNoPermitidaException(
                    "La orden no está pendiente de preparación de presupuesto");
        }

        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setOrden(orden);
        presupuestoRepository.save(presupuesto);
        presupuestoRepository.flush();
        entityManager.refresh(presupuesto);

        if (orden.getEstado() == EstadoOrden.solicitud_recibida) {
            Usuario autor = usuarioRepository.findById(SecurityUtils.idActual())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
            ordenEstadoService.aplicarCambio(
                    orden, EstadoOrden.pendiente_presupuesto,
                    "Solicitud revisada, preparando presupuesto", autor);
        }
        return respuesta(presupuesto, true);
    }

    @Transactional(readOnly = true)
    public PresupuestoResponse obtenerPorOrden(Integer ordenId) {
        OrdenTrabajo orden = obtenerOrden(ordenId);
        Presupuesto presupuesto = presupuestoRepository.findByOrdenId(ordenId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Presupuesto no encontrado"));
        boolean adminOMecanico = SecurityUtils.esAdmin() || SecurityUtils.esMecanico();
        boolean propietario = orden.getVehiculo().getUsuario().getId().equals(SecurityUtils.idActual());
        boolean mecanicoAsignado = orden.getMecanico() != null
                && orden.getMecanico().getId().equals(SecurityUtils.idActual());
        if (!SecurityUtils.esAdmin() && !(SecurityUtils.esMecanico() && mecanicoAsignado)
                && !(SecurityUtils.esCliente() && propietario
                        && presupuesto.getEstado() != EstadoPresupuesto.borrador)) {
            throw new RecursoNoEncontradoException("Presupuesto no encontrado");
        }
        return respuesta(presupuesto, adminOMecanico);
    }

    @Transactional
    public PresupuestoResponse actualizar(Integer id, PresupuestoUpdateRequest solicitud) {
        Presupuesto presupuesto = obtenerEditable(id);
        if (solicitud.tiempoEstimadoHoras() != null) {
            presupuesto.setTiempoEstimadoHoras(solicitud.tiempoEstimadoHoras());
        }
        if (solicitud.garantiaMeses() != null) {
            presupuesto.setGarantiaMeses(solicitud.garantiaMeses());
        }
        return respuesta(presupuestoRepository.save(presupuesto), true);
    }

    @Transactional
    public LineaCreadaResponse anadirLinea(Integer id, LineaRequest solicitud) {
        Presupuesto presupuesto = obtenerEditable(id);
        LineaPresupuesto linea = prepararLinea(presupuesto, solicitud);
        lineaRepository.save(linea);
        return guardarLineaYResponder(presupuesto, linea);
    }

    @Transactional
    public LineaCreadaResponse modificarLinea(Integer id, Integer lineaId, LineaRequest solicitud) {
        Presupuesto presupuesto = obtenerEditable(id);
        LineaPresupuesto linea = lineaRepository.findByIdAndPresupuestoId(lineaId, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Línea no encontrada"));
        aplicarLinea(linea, solicitud);
        return guardarLineaYResponder(presupuesto, linea);
    }

    @Transactional
    public TotalesResponse eliminarLinea(Integer id, Integer lineaId) {
        Presupuesto presupuesto = obtenerEditable(id);
        LineaPresupuesto linea = lineaRepository.findByIdAndPresupuestoId(lineaId, id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Línea no encontrada"));
        lineaRepository.delete(linea);
        lineaRepository.flush();
        entityManager.refresh(presupuesto);
        return mapper.aTotalesResponse(presupuesto);
    }

    @Transactional
    public PresupuestoResponse enviar(Integer id) {
        exigirAdministrador();
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Presupuesto no encontrado"));
        exigirEnBorrador(presupuesto);
        if (lineaRepository.countByPresupuestoId(id) == 0) {
            throw new SolicitudInvalidaException("El presupuesto no tiene líneas");
        }

        presupuesto.setEstado(EstadoPresupuesto.enviado);
        presupuesto.setEnviadoEn(LocalDateTime.now());
        presupuestoRepository.save(presupuesto);

        Usuario administrador = usuarioRepository.findById(SecurityUtils.idActual())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        ordenEstadoService.aplicarCambio(
                presupuesto.getOrden(),
                EstadoOrden.presupuesto_enviado,
                "Presupuesto enviado al cliente",
                administrador);
        String total = NumberFormat.getNumberInstance(new Locale("es", "ES"))
                .format(presupuesto.getTotal());
        notificacionService.notificarCliente(
                presupuesto.getOrden(),
                com.motorlab.entity.enums.TipoNotificacion.presupuesto_listo,
                "Tu presupuesto está listo · " + total + " €");
        return respuesta(presupuesto, true);
    }

    @Transactional
    public PresupuestoResponse autorizar(Integer id) {
        Presupuesto presupuesto = obtenerPresupuestoBloqueadoYPropietario(id);
        exigirEnviado(presupuesto);

        presupuesto.setEstado(EstadoPresupuesto.autorizado);
        presupuesto.setRespondidoEn(LocalDateTime.now());
        presupuestoRepository.save(presupuesto);
        Usuario cliente = usuarioRepository.findById(SecurityUtils.idActual())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        ordenEstadoService.aplicarCambio(
                presupuesto.getOrden(),
                EstadoOrden.presupuesto_autorizado,
                "Presupuesto autorizado por el cliente",
                cliente);
        notificacionService.notificarAdmins(
                presupuesto.getOrden(),
                com.motorlab.entity.enums.TipoNotificacion.presupuesto_respondido,
                "El cliente ha autorizado el presupuesto");
        return respuesta(presupuesto, false);
    }

    @Transactional
    public PresupuestoResponse rechazar(Integer id, RechazoRequest solicitud) {
        Presupuesto presupuesto = obtenerPresupuestoBloqueadoYPropietario(id);
        exigirEnviado(presupuesto);

        presupuesto.setEstado(EstadoPresupuesto.rechazado);
        presupuesto.setRespondidoEn(LocalDateTime.now());
        presupuesto.setMotivoRechazo(solicitud.motivo());
        presupuestoRepository.save(presupuesto);
        Usuario cliente = usuarioRepository.findById(SecurityUtils.idActual())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        ordenEstadoService.aplicarCambio(
                presupuesto.getOrden(),
                EstadoOrden.rechazada,
                "Presupuesto rechazado por el cliente",
                cliente);
        notificacionService.notificarAdmins(
                presupuesto.getOrden(),
                com.motorlab.entity.enums.TipoNotificacion.presupuesto_respondido,
                "El cliente ha rechazado el presupuesto");
        return respuesta(presupuesto, false);
    }

    private LineaCreadaResponse guardarLineaYResponder(Presupuesto presupuesto, LineaPresupuesto linea) {
        lineaRepository.flush();
        entityManager.refresh(presupuesto);
        entityManager.refresh(linea);
        AvisoStockResponse aviso = linea.getPieza() == null ? null : avisoDeStock(presupuesto, linea.getPieza());
        return new LineaCreadaResponse(
                mapper.aLineaResponse(linea), mapper.aTotalesResponse(presupuesto), aviso);
    }

    private LineaPresupuesto prepararLinea(Presupuesto presupuesto, LineaRequest solicitud) {
        LineaPresupuesto linea = new LineaPresupuesto();
        linea.setPresupuesto(presupuesto);
        aplicarLinea(linea, solicitud);
        return linea;
    }

    private void aplicarLinea(LineaPresupuesto linea, LineaRequest solicitud) {
        List<ErrorDetalle> detalles = new ArrayList<>();
        linea.setTipo(solicitud.tipo());
        if (solicitud.tipo() == null) {
            detalles.add(new ErrorDetalle("tipo", "El tipo es obligatorio"));
        } else if (solicitud.tipo() == TipoLinea.pieza) {
            if (solicitud.piezaId() == null) {
                detalles.add(new ErrorDetalle("piezaId", "La pieza es obligatoria para una línea de pieza"));
            } else {
                Pieza pieza = piezaRepository.findById(solicitud.piezaId())
                        .filter(Pieza::isActiva)
                        .orElse(null);
                if (pieza == null) {
                    detalles.add(new ErrorDetalle("piezaId", "La pieza no existe o no está activa"));
                } else {
                    linea.setPieza(pieza);
                    linea.setConcepto(pieza.getNombre());
                    linea.setPrecioUnitario(pieza.getPrecio());
                }
            }
            if (solicitud.cantidad() != null
                    && solicitud.cantidad().stripTrailingZeros().scale() > 0) {
                detalles.add(new ErrorDetalle("cantidad", "La cantidad de una pieza debe ser un entero"));
            }
        } else if (solicitud.tipo() == TipoLinea.mano_obra) {
            if (solicitud.piezaId() != null) {
                detalles.add(new ErrorDetalle("piezaId", "La mano de obra no puede tener pieza"));
            }
            if (solicitud.concepto() == null || solicitud.concepto().isBlank()) {
                detalles.add(new ErrorDetalle("concepto", "El concepto es obligatorio para mano de obra"));
            } else {
                linea.setConcepto(solicitud.concepto().trim());
            }
            if (solicitud.precioUnitario() == null
                    || solicitud.precioUnitario().compareTo(BigDecimal.ZERO) < 0) {
                detalles.add(new ErrorDetalle(
                        "precioUnitario", "El precio unitario es obligatorio y no puede ser negativo"));
            } else {
                linea.setPrecioUnitario(solicitud.precioUnitario());
            }
            linea.setPieza(null);
        }
        linea.setCantidad(solicitud.cantidad());
        if (!detalles.isEmpty()) {
            throw new SolicitudInvalidaException("La línea no es válida", detalles);
        }
    }

    private AvisoStockResponse avisoDeStock(Presupuesto presupuesto, Pieza pieza) {
        BigDecimal necesarias = lineaRepository.findByPresupuestoIdOrderByIdAsc(presupuesto.getId()).stream()
                .filter(linea -> linea.getPieza() != null
                        && linea.getPieza().getId().equals(pieza.getId()))
                .map(LineaPresupuesto::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return pieza.getStock() < necesarias.intValue()
                || BigDecimal.valueOf(pieza.getStock()).compareTo(necesarias) < 0
                        ? new AvisoStockResponse(pieza.getId(), pieza.getReferencia(), pieza.getStock(), necesarias)
                        : null;
    }

    private Presupuesto obtenerEditable(Integer id) {
        exigirAdministrador();
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Presupuesto no encontrado"));
        if (presupuesto.getEstado() != EstadoPresupuesto.borrador) {
            throw new PresupuestoNoEditableException();
        }
        return presupuesto;
    }

    private Presupuesto obtenerPresupuestoBloqueadoYPropietario(Integer id) {
        Presupuesto presupuesto = presupuestoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Presupuesto no encontrado"));
        if (!SecurityUtils.esCliente()
                || !presupuesto.getOrden().getVehiculo().getUsuario().getId().equals(SecurityUtils.idActual())) {
            throw new RecursoNoEncontradoException("Presupuesto no encontrado");
        }
        return presupuesto;
    }

    private void exigirEnBorrador(Presupuesto presupuesto) {
        if (presupuesto.getEstado() != EstadoPresupuesto.borrador) {
            throw new PresupuestoNoEditableException();
        }
    }

    private void exigirEnviado(Presupuesto presupuesto) {
        if (presupuesto.getEstado() != EstadoPresupuesto.enviado) {
            throw new TransicionNoPermitidaException("El presupuesto no está pendiente de respuesta");
        }
    }

    private PresupuestoResponse respuesta(Presupuesto presupuesto, boolean incluirAvisos) {
        List<LineaPresupuesto> lineas = lineaRepository.findByPresupuestoIdOrderByIdAsc(presupuesto.getId());
        return mapper.aResponse(presupuesto, lineas, incluirAvisos ? avisosDeStock(lineas) : List.of());
    }

    private List<AvisoStockResponse> avisosDeStock(List<LineaPresupuesto> lineas) {
        Map<Integer, BigDecimal> necesarias = new LinkedHashMap<>();
        Map<Integer, Pieza> piezas = new LinkedHashMap<>();
        for (LineaPresupuesto linea : lineas) {
            if (linea.getPieza() != null) {
                Integer piezaId = linea.getPieza().getId();
                necesarias.merge(piezaId, linea.getCantidad(), BigDecimal::add);
                piezas.put(piezaId, linea.getPieza());
            }
        }
        return necesarias.entrySet().stream()
                .filter(entrada -> BigDecimal.valueOf(piezas.get(entrada.getKey()).getStock())
                        .compareTo(entrada.getValue()) < 0)
                .map(entrada -> {
                    Pieza pieza = piezas.get(entrada.getKey());
                    return new AvisoStockResponse(
                            pieza.getId(), pieza.getReferencia(), pieza.getStock(), entrada.getValue());
                })
                .toList();
    }

    private OrdenTrabajo obtenerOrden(Integer id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
    }

    private void exigirAdministrador() {
        if (!SecurityUtils.esAdmin()) {
            throw new SolicitudInvalidaException("Solo un administrador puede gestionar presupuestos");
        }
    }
}
