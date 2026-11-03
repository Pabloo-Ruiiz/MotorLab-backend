package com.motorlab.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.dto.panel.MetricasResponse;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.EstadoPresupuesto;
import com.motorlab.repository.OrdenTrabajoRepository;
import com.motorlab.repository.PiezaRepository;
import com.motorlab.repository.PresupuestoRepository;

@Service
public class PanelService {

    private static final List<EstadoOrden> ESTADOS_CERRADOS = List.of(
            EstadoOrden.entregado,
            EstadoOrden.rechazada,
            EstadoOrden.cancelada);

    private final OrdenTrabajoRepository ordenTrabajoRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final PiezaRepository piezaRepository;
    private final int umbralStockBajo;

    public PanelService(
            OrdenTrabajoRepository ordenTrabajoRepository,
            PresupuestoRepository presupuestoRepository,
            PiezaRepository piezaRepository,
            @Value("${app.stock-bajo-umbral}") int umbralStockBajo) {
        this.ordenTrabajoRepository = ordenTrabajoRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.piezaRepository = piezaRepository;
        this.umbralStockBajo = umbralStockBajo;
    }

    @Transactional(readOnly = true)
    public MetricasResponse obtenerMetricas() {
        LocalDate primerDiaMes = LocalDate.now().withDayOfMonth(1);
        LocalDate primerDiaSiguiente = primerDiaMes.plusMonths(1);
        Map<EstadoOrden, Long> ordenesPorEstado = new EnumMap<>(EstadoOrden.class);
        for (EstadoOrden estado : EstadoOrden.values()) {
            ordenesPorEstado.put(estado, 0L);
        }
        for (Object[] fila : ordenTrabajoRepository.contarPorEstado()) {
            ordenesPorEstado.put((EstadoOrden) fila[0], ((Number) fila[1]).longValue());
        }

        BigDecimal ingresosMes = presupuestoRepository.sumarAutorizadosEntre(
                primerDiaMes.atStartOfDay(),
                primerDiaSiguiente.atStartOfDay());
        return new MetricasResponse(
                ordenTrabajoRepository.countByEstadoNotIn(ESTADOS_CERRADOS),
                ordenesPorEstado,
                ingresosMes,
                presupuestoRepository.countByEstado(EstadoPresupuesto.enviado),
                piezaRepository.countByActivaTrueAndStockLessThanEqual(umbralStockBajo));
    }
}
