package com.motorlab.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.motorlab.entity.enums.EstadoOrden;

class TransicionesEstadoTest {

    private final TransicionesEstado transiciones = new TransicionesEstado();

    @Test
    void permiteTodasLasTransicionesValidas() {
        assertTrue(transiciones.permitida(
                EstadoOrden.solicitud_recibida, EstadoOrden.pendiente_presupuesto));
        assertTrue(transiciones.permitida(
                EstadoOrden.solicitud_recibida, EstadoOrden.cancelada));
        assertTrue(transiciones.permitida(
                EstadoOrden.pendiente_presupuesto, EstadoOrden.presupuesto_enviado));
        assertTrue(transiciones.permitida(
                EstadoOrden.pendiente_presupuesto, EstadoOrden.cancelada));
        assertTrue(transiciones.permitida(
                EstadoOrden.presupuesto_enviado, EstadoOrden.presupuesto_autorizado));
        assertTrue(transiciones.permitida(
                EstadoOrden.presupuesto_enviado, EstadoOrden.rechazada));
        assertTrue(transiciones.permitida(
                EstadoOrden.presupuesto_enviado, EstadoOrden.cancelada));
        assertTrue(transiciones.permitida(
                EstadoOrden.presupuesto_autorizado, EstadoOrden.recibido));
        assertTrue(transiciones.permitida(
                EstadoOrden.presupuesto_autorizado, EstadoOrden.cancelada));
        assertTrue(transiciones.permitida(
                EstadoOrden.recibido, EstadoOrden.en_preparacion));
        assertTrue(transiciones.permitida(
                EstadoOrden.en_preparacion, EstadoOrden.esperando_pieza));
        assertTrue(transiciones.permitida(
                EstadoOrden.en_preparacion, EstadoOrden.arreglando));
        assertTrue(transiciones.permitida(
                EstadoOrden.esperando_pieza, EstadoOrden.arreglando));
        assertTrue(transiciones.permitida(
                EstadoOrden.arreglando, EstadoOrden.esperando_pieza));
        assertTrue(transiciones.permitida(
                EstadoOrden.arreglando, EstadoOrden.listo));
        assertTrue(transiciones.permitida(EstadoOrden.listo, EstadoOrden.entregado));
    }

    @Test
    void rechazaTransicionesNoValidas() {
        assertFalse(transiciones.permitida(EstadoOrden.listo, EstadoOrden.arreglando));
        assertFalse(transiciones.permitida(EstadoOrden.recibido, EstadoOrden.listo));

        for (EstadoOrden estado : EstadoOrden.values()) {
            assertFalse(transiciones.permitida(EstadoOrden.entregado, estado));
            assertFalse(transiciones.permitida(EstadoOrden.cancelada, estado));
        }
    }
}
