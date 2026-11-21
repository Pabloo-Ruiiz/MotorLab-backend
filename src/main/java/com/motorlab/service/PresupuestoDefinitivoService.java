package com.motorlab.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.dto.presupuesto.PresupuestoDefinitivoResponse;
import com.motorlab.entity.LineaPresupuesto;
import com.motorlab.entity.LineaPresupuestoDefinitivo;
import com.motorlab.entity.OrdenTrabajo;
import com.motorlab.entity.Presupuesto;
import com.motorlab.entity.PresupuestoDefinitivo;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.Vehiculo;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.EstadoPresupuesto;
import com.motorlab.entity.enums.TipoLinea;
import com.motorlab.exception.RecursoNoEncontradoException;
import com.motorlab.exception.TransicionNoPermitidaException;
import com.motorlab.mapper.PresupuestoDefinitivoMapper;
import com.motorlab.repository.LineaPresupuestoRepository;
import com.motorlab.repository.PresupuestoDefinitivoRepository;
import com.motorlab.repository.PresupuestoRepository;

/**
 * Emite el presupuesto definitivo cuando la reparación finaliza (orden en 'listo') y lo sirve al
 * cliente, al administrador y al mecánico asignado. Antes de esa fecha no existe para nadie.
 */
@Service
public class PresupuestoDefinitivoService {

    // 'entregado' solo se alcanza desde 'listo': la reparación también está finalizada
    // Listo para recoger o ya entregado. También marca cuándo puede pagar el cliente
    static final Set<EstadoOrden> REPARACION_FINALIZADA = EnumSet.of(EstadoOrden.listo, EstadoOrden.entregado);

    private final PresupuestoDefinitivoRepository definitivoRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final LineaPresupuestoRepository lineaRepository;
    private final PresupuestoDefinitivoMapper mapper;
    private final PresupuestoDefinitivoPdf pdf;
    private final OrdenService ordenService;

    public PresupuestoDefinitivoService(
            PresupuestoDefinitivoRepository definitivoRepository,
            PresupuestoRepository presupuestoRepository,
            LineaPresupuestoRepository lineaRepository,
            PresupuestoDefinitivoMapper mapper,
            PresupuestoDefinitivoPdf pdf,
            // Perezoso: OrdenService depende, a través de OrdenEstadoService, de este servicio
            @Lazy OrdenService ordenService) {
        this.definitivoRepository = definitivoRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.lineaRepository = lineaRepository;
        this.mapper = mapper;
        this.pdf = pdf;
        this.ordenService = ordenService;
    }

    /**
     * Emite el presupuesto de una orden que acaba de finalizar. Si ya existe lo devuelve tal cual:
     * nunca crea un segundo documento ni recalcula los importes de uno emitido.
     */
    @Transactional
    public PresupuestoDefinitivo generar(OrdenTrabajo orden) {
        if (!REPARACION_FINALIZADA.contains(orden.getEstado())) {
            throw new TransicionNoPermitidaException(
                    "El presupuesto definitivo solo se emite cuando la reparación ha finalizado");
        }
        var existente = definitivoRepository.findByOrdenId(orden.getId());
        if (existente.isPresent()) {
            return existente.get();
        }

        Integer presupuestoId = presupuestoRepository.findByOrdenId(orden.getId())
                .map(Presupuesto::getId)
                .orElseThrow(() -> new TransicionNoPermitidaException("La orden no tiene un presupuesto asociado"));
        // El bloqueo ordena dos emisiones simultáneas de la misma orden: la segunda ve el documento de la primera
        Presupuesto presupuesto = presupuestoRepository.findByIdForUpdate(presupuestoId).orElseThrow();
        existente = definitivoRepository.findByOrdenId(orden.getId());
        if (existente.isPresent()) {
            return existente.get();
        }
        if (presupuesto.getEstado() != EstadoPresupuesto.autorizado) {
            throw new TransicionNoPermitidaException("El presupuesto de la orden no está autorizado");
        }

        PresupuestoDefinitivo definitivo = copiar(orden, presupuesto,
                lineaRepository.findByPresupuestoIdOrderByIdAsc(presupuesto.getId()));
        return definitivoRepository.saveAndFlush(definitivo);
    }

    @Transactional
    public PresupuestoDefinitivoResponse consultar(Integer ordenId) {
        return mapper.aResponse(obtenerOEmitir(ordenId));
    }

    @Transactional
    public DocumentoPdf descargar(Integer ordenId) {
        PresupuestoDefinitivo definitivo = obtenerOEmitir(ordenId);
        return new DocumentoPdf(
                "presupuesto-" + definitivo.getNumero() + ".pdf",
                pdf.generar(definitivo));
    }

    // Las órdenes que ya habían finalizado antes de existir esta función reciben su documento al pedirlo
    private PresupuestoDefinitivo obtenerOEmitir(Integer ordenId) {
        OrdenTrabajo orden = ordenService.obtenerEntidadAutorizada(ordenId);
        if (!REPARACION_FINALIZADA.contains(orden.getEstado())) {
            throw new RecursoNoEncontradoException(
                    "El presupuesto definitivo estará disponible cuando finalice la reparación");
        }
        return definitivoRepository.findByOrdenId(ordenId).orElseGet(() -> generar(orden));
    }

    private PresupuestoDefinitivo copiar(OrdenTrabajo orden, Presupuesto presupuesto, List<LineaPresupuesto> lineas) {
        Vehiculo vehiculo = orden.getVehiculo();
        Usuario cliente = vehiculo.getUsuario();

        PresupuestoDefinitivo definitivo = new PresupuestoDefinitivo();
        definitivo.setOrden(orden);
        definitivo.setPresupuesto(presupuesto);
        definitivo.setEmitidoEn(LocalDateTime.now());
        definitivo.setClienteNombre(cliente.getNombre());
        definitivo.setClienteEmail(cliente.getEmail());
        definitivo.setClienteTelefono(cliente.getTelefono());
        definitivo.setVehiculoMatricula(vehiculo.getMatricula());
        definitivo.setVehiculoMarca(vehiculo.getMarca());
        definitivo.setVehiculoModelo(vehiculo.getModelo());
        definitivo.setVehiculoVersion(vehiculo.getVersion());
        definitivo.setVehiculoAnio(vehiculo.getAnio());
        definitivo.setVehiculoKilometros(vehiculo.getKilometros());
        definitivo.setTipoServicio(orden.getTipoServicio());

        BigDecimal manoObra = BigDecimal.ZERO;
        BigDecimal piezas = BigDecimal.ZERO;
        for (LineaPresupuesto origen : lineas) {
            LineaPresupuestoDefinitivo linea = new LineaPresupuestoDefinitivo();
            linea.setTipo(origen.getTipo());
            linea.setReferencia(origen.getPieza() == null ? null : origen.getPieza().getReferencia());
            linea.setConcepto(origen.getConcepto());
            linea.setCantidad(origen.getCantidad());
            linea.setPrecioUnitario(origen.getPrecioUnitario());
            linea.setImporte(origen.getImporte());
            definitivo.anadirLinea(linea);
            if (origen.getTipo() == TipoLinea.mano_obra) {
                manoObra = manoObra.add(origen.getImporte());
            } else {
                piezas = piezas.add(origen.getImporte());
            }
        }

        // Los totales los calcula la base de datos: el documento debe cuadrar con sus líneas
        if (manoObra.add(piezas).compareTo(presupuesto.getSubtotal()) != 0) {
            throw new IllegalStateException("Las líneas del presupuesto " + presupuesto.getId()
                    + " no suman su base imponible: no se emite el presupuesto definitivo");
        }
        definitivo.setSubtotalManoObra(manoObra);
        definitivo.setSubtotalPiezas(piezas);
        definitivo.setSubtotal(presupuesto.getSubtotal());
        definitivo.setIvaPorcentaje(presupuesto.getIvaPorcentaje());
        definitivo.setIva(presupuesto.getIva());
        definitivo.setTotal(presupuesto.getTotal());
        return definitivo;
    }

    public record DocumentoPdf(String nombreArchivo, byte[] contenido) {
    }
}
