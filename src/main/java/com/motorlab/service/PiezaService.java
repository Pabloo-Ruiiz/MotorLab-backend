package com.motorlab.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.dto.common.ErrorDetalle;
import com.motorlab.dto.common.PageResponse;
import com.motorlab.dto.pieza.PiezaRequest;
import com.motorlab.dto.pieza.PiezaResponse;
import com.motorlab.entity.LineaPresupuesto;
import com.motorlab.entity.Pieza;
import com.motorlab.entity.enums.TipoLinea;
import com.motorlab.exception.ConflictoException;
import com.motorlab.exception.RecursoNoEncontradoException;
import com.motorlab.exception.SolicitudInvalidaException;
import com.motorlab.exception.StockInsuficienteException;
import com.motorlab.mapper.PiezaMapper;
import com.motorlab.repository.LineaPresupuestoRepository;
import com.motorlab.repository.PiezaRepository;

@Service
public class PiezaService {

    private static final Logger LOG = LoggerFactory.getLogger(PiezaService.class);

    private final PiezaRepository piezaRepository;
    private final LineaPresupuestoRepository lineaPresupuestoRepository;
    private final PiezaMapper piezaMapper;
    private final int umbralStockBajo;

    public PiezaService(
            PiezaRepository piezaRepository,
            LineaPresupuestoRepository lineaPresupuestoRepository,
            PiezaMapper piezaMapper,
            @Value("${app.stock-bajo-umbral}") int umbralStockBajo) {
        this.piezaRepository = piezaRepository;
        this.lineaPresupuestoRepository = lineaPresupuestoRepository;
        this.piezaMapper = piezaMapper;
        this.umbralStockBajo = umbralStockBajo;
    }

    @Transactional(readOnly = true)
    public PageResponse<PiezaResponse> listar(
            String q, boolean soloActivas, boolean stockBajo, Integer pagina, Integer limite) {
        Pageable pageable = PageResponse.pageable(pagina, limite);
        Page<Pieza> piezas = piezaRepository.buscar(
                q == null ? "" : q,
                soloActivas,
                stockBajo,
                umbralStockBajo,
                pageable);
        return PageResponse.de(piezas.map(piezaMapper::aResponse));
    }

    @Transactional
    public PiezaResponse crear(PiezaRequest solicitud) {
        String referencia = solicitud.referencia().trim();
        if (piezaRepository.existsByReferencia(referencia)) {
            throw new ConflictoException("La referencia ya está registrada");
        }
        Pieza pieza = new Pieza();
        pieza.setReferencia(referencia);
        pieza.setNombre(solicitud.nombre().trim());
        pieza.setPrecio(solicitud.precio());
        pieza.setStock(solicitud.stock() == null ? 0 : solicitud.stock());
        pieza.setActiva(true);
        return piezaMapper.aResponse(piezaRepository.save(pieza));
    }

    @Transactional(readOnly = true)
    public PiezaResponse obtener(Integer id) {
        return piezaMapper.aResponse(obtenerPieza(id));
    }

    @Transactional
    public PiezaResponse actualizar(Integer id, PiezaRequest solicitud) {
        Pieza pieza = obtenerPieza(id);
        String referencia = solicitud.referencia().trim();
        if (!referencia.equals(pieza.getReferencia()) && piezaRepository.existsByReferencia(referencia)) {
            throw new ConflictoException("La referencia ya está registrada");
        }
        pieza.setReferencia(referencia);
        pieza.setNombre(solicitud.nombre().trim());
        pieza.setPrecio(solicitud.precio());
        return piezaMapper.aResponse(pieza);
    }

    @Transactional
    public void darDeBaja(Integer id) {
        obtenerPieza(id).setActiva(false);
    }

    @Transactional
    public PiezaResponse ajustarStock(Integer id, Integer delta, String motivo) {
        Pieza pieza = piezaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pieza no encontrada"));
        LOG.info("Ajuste de stock de la pieza {}: delta={}, motivo={}", id, delta, motivo);
        long stockNuevo = (long) pieza.getStock() + delta;
        if (stockNuevo < 0) {
            throw insuficiente(pieza, -delta);
        }
        if (stockNuevo > Integer.MAX_VALUE) {
            throw new SolicitudInvalidaException("El stock resultante supera el límite permitido");
        }
        pieza.setStock((int) stockNuevo);
        return piezaMapper.aResponse(pieza);
    }

    @Transactional
    public void descontarStockDePresupuesto(Integer presupuestoId) {
        List<LineaPresupuesto> lineas = lineaPresupuestoRepository
                .findByPresupuestoIdOrderByIdAsc(presupuestoId);
        Map<Integer, BigDecimal> cantidades = new HashMap<>();
        for (LineaPresupuesto linea : lineas) {
            if (linea.getTipo() == TipoLinea.pieza && linea.getPieza() != null) {
                cantidades.merge(linea.getPieza().getId(), linea.getCantidad(), BigDecimal::add);
            }
        }
        if (cantidades.isEmpty()) {
            return;
        }

        List<Integer> ids = new ArrayList<>(cantidades.keySet());
        ids.sort(Comparator.naturalOrder());
        List<Pieza> piezas = piezaRepository.findAllByIdForUpdate(ids);
        Map<Integer, Pieza> piezasPorId = new HashMap<>();
        piezas.forEach(pieza -> piezasPorId.put(pieza.getId(), pieza));

        List<ErrorDetalle> detalles = new ArrayList<>();
        for (Integer id : ids) {
            Pieza pieza = piezasPorId.get(id);
            if (pieza == null) {
                throw new RecursoNoEncontradoException("Pieza no encontrada");
            }
            BigDecimal necesaria = cantidades.get(id);
            if (necesaria.compareTo(BigDecimal.valueOf(pieza.getStock())) > 0) {
                detalles.add(new ErrorDetalle(
                        "pieza_" + id,
                        "Stock " + pieza.getStock() + ", necesarias " + necesaria.stripTrailingZeros().toPlainString()));
            }
        }
        if (!detalles.isEmpty()) {
            throw new StockInsuficienteException(detalles);
        }

        for (Integer id : ids) {
            Pieza pieza = piezasPorId.get(id);
            int cantidad = cantidadEntera(cantidades.get(id));
            pieza.setStock(pieza.getStock() - cantidad);
        }
    }

    private Pieza obtenerPieza(Integer id) {
        return piezaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pieza no encontrada"));
    }

    private StockInsuficienteException insuficiente(Pieza pieza, int necesarias) {
        return new StockInsuficienteException(List.of(new ErrorDetalle(
                "stock",
                "Stock " + pieza.getStock() + ", necesarias " + necesarias)));
    }

    private int cantidadEntera(BigDecimal cantidad) {
        try {
            return cantidad.intValueExact();
        } catch (ArithmeticException excepcion) {
            throw new SolicitudInvalidaException("La cantidad de piezas debe ser un número entero");
        }
    }
}
