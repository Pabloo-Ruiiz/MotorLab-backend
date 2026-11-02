package com.motorlab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.motorlab.entity.LineaPresupuesto;
import com.motorlab.entity.Pieza;
import com.motorlab.entity.enums.TipoLinea;
import com.motorlab.exception.StockInsuficienteException;
import com.motorlab.mapper.PiezaMapper;
import com.motorlab.repository.LineaPresupuestoRepository;
import com.motorlab.repository.PiezaRepository;

class PiezaServiceTest {

    private PiezaRepository piezaRepository;
    private LineaPresupuestoRepository lineaPresupuestoRepository;
    private PiezaService piezaService;

    @BeforeEach
    void prepararServicio() {
        piezaRepository = mock(PiezaRepository.class);
        lineaPresupuestoRepository = mock(LineaPresupuestoRepository.class);
        piezaService = new PiezaService(
                piezaRepository,
                lineaPresupuestoRepository,
                new PiezaMapper(2),
                2);
    }

    @Test
    void ajustarStockPorDebajoDeCeroLanzaExcepcion() {
        Pieza pieza = pieza(1, 0);
        when(piezaRepository.findByIdForUpdate(1)).thenReturn(Optional.of(pieza));

        assertThrows(
                StockInsuficienteException.class,
                () -> piezaService.ajustarStock(1, -1, "Corrección"));
        assertEquals(0, pieza.getStock());
    }

    @Test
    void ajustePositivoSumaStock() {
        Pieza pieza = pieza(1, 1);
        when(piezaRepository.findByIdForUpdate(1)).thenReturn(Optional.of(pieza));

        piezaService.ajustarStock(1, 4, "Entrada de pedido");

        assertEquals(5, pieza.getStock());
    }

    @Test
    void descontarPresupuestoFallaSinDescontarLasDemasPiezas() {
        Pieza pastillas = pieza(1, 4);
        Pieza discos = pieza(2, 1);
        Pieza liquido = pieza(3, 10);
        when(lineaPresupuestoRepository.findByPresupuestoIdOrderByIdAsc(1))
                .thenReturn(List.of(
                        linea(1, pastillas, "1"),
                        linea(2, discos, "2"),
                        linea(3, liquido, "1")));
        when(piezaRepository.findAllByIdForUpdate(anyList()))
                .thenReturn(List.of(pastillas, discos, liquido));

        assertThrows(
                StockInsuficienteException.class,
                () -> piezaService.descontarStockDePresupuesto(1));

        assertEquals(4, pastillas.getStock());
        assertEquals(1, discos.getStock());
        assertEquals(10, liquido.getStock());
        verify(piezaRepository).findAllByIdForUpdate(eq(List.of(1, 2, 3)));
    }

    private Pieza pieza(int id, int stock) {
        Pieza pieza = new Pieza();
        pieza.setId(id);
        pieza.setReferencia("REF-" + id);
        pieza.setStock(stock);
        return pieza;
    }

    private LineaPresupuesto linea(int id, Pieza pieza, String cantidad) {
        LineaPresupuesto linea = new LineaPresupuesto();
        linea.setId(id);
        linea.setTipo(TipoLinea.pieza);
        linea.setPieza(pieza);
        linea.setCantidad(new BigDecimal(cantidad));
        return linea;
    }
}
