package com.motorlab.service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.motorlab.entity.enums.EstadoOrden;

@Component
public class TransicionesEstado {

    public static final Set<EstadoOrden> ESTADOS_MECANICO = Set.of(
            EstadoOrden.en_preparacion,
            EstadoOrden.esperando_pieza,
            EstadoOrden.arreglando,
            EstadoOrden.listo);

    public static final Set<EstadoOrden> ESTADOS_AUTOMATICOS = Set.of(
            EstadoOrden.presupuesto_enviado,
            EstadoOrden.presupuesto_autorizado,
            EstadoOrden.rechazada,
            EstadoOrden.cancelada);

    private final Map<EstadoOrden, Set<EstadoOrden>> transiciones;

    public TransicionesEstado() {
        EnumMap<EstadoOrden, Set<EstadoOrden>> mapa = new EnumMap<>(EstadoOrden.class);
        mapa.put(EstadoOrden.solicitud_recibida,
                Set.of(EstadoOrden.pendiente_presupuesto, EstadoOrden.cancelada));
        mapa.put(EstadoOrden.pendiente_presupuesto,
                Set.of(EstadoOrden.presupuesto_enviado, EstadoOrden.cancelada));
        mapa.put(EstadoOrden.presupuesto_enviado,
                Set.of(EstadoOrden.presupuesto_autorizado, EstadoOrden.rechazada, EstadoOrden.cancelada));
        mapa.put(EstadoOrden.presupuesto_autorizado,
                Set.of(EstadoOrden.recibido, EstadoOrden.cancelada));
        mapa.put(EstadoOrden.recibido, Set.of(EstadoOrden.en_preparacion));
        mapa.put(EstadoOrden.en_preparacion,
                Set.of(EstadoOrden.esperando_pieza, EstadoOrden.arreglando));
        mapa.put(EstadoOrden.esperando_pieza, Set.of(EstadoOrden.arreglando));
        mapa.put(EstadoOrden.arreglando,
                Set.of(EstadoOrden.esperando_pieza, EstadoOrden.listo));
        mapa.put(EstadoOrden.listo, Set.of(EstadoOrden.entregado));
        mapa.put(EstadoOrden.entregado, Set.of());
        mapa.put(EstadoOrden.rechazada, Set.of());
        mapa.put(EstadoOrden.cancelada, Set.of());
        transiciones = Map.copyOf(mapa);
    }

    public boolean permitida(EstadoOrden desde, EstadoOrden hasta) {
        return desde != null && hasta != null
                && siguientes(desde).contains(hasta);
    }

    public Set<EstadoOrden> siguientes(EstadoOrden desde) {
        return desde == null ? Set.of() : transiciones.getOrDefault(desde, Set.of());
    }
}
