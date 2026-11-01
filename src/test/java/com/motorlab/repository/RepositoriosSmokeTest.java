package com.motorlab.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.motorlab.entity.OrdenResumen;
import com.motorlab.entity.Pieza;
import com.motorlab.entity.Presupuesto;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.Rol;
import com.motorlab.entity.enums.Urgencia;
import com.motorlab.support.IntegracionBase;

class RepositoriosSmokeTest extends IntegracionBase {

    private final UsuarioRepository usuarioRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final OrdenResumenRepository ordenResumenRepository;
    private final LineaPresupuestoRepository lineaPresupuestoRepository;
    private final PiezaRepository piezaRepository;

    @Autowired
    RepositoriosSmokeTest(
            UsuarioRepository usuarioRepository,
            PresupuestoRepository presupuestoRepository,
            OrdenResumenRepository ordenResumenRepository,
            LineaPresupuestoRepository lineaPresupuestoRepository,
            PiezaRepository piezaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.ordenResumenRepository = ordenResumenRepository;
        this.lineaPresupuestoRepository = lineaPresupuestoRepository;
        this.piezaRepository = piezaRepository;
    }

    @Test
    void compruebaDatosDeEjemplo() {
        assertEquals(3, usuarioRepository.count());

        Usuario cliente = usuarioRepository.findByEmail("laura@cliente.example").orElseThrow();
        assertEquals(Rol.cliente, cliente.getRol());

        Presupuesto presupuesto = presupuestoRepository.findByOrdenId(2481).orElseThrow();
        assertEquals(0, presupuesto.getSubtotal().compareTo(new BigDecimal("212.00")));
        assertEquals(0, presupuesto.getIva().compareTo(new BigDecimal("44.52")));
        assertEquals(0, presupuesto.getTotal().compareTo(new BigDecimal("256.52")));

        OrdenResumen resumen = ordenResumenRepository.findById(2481).orElseThrow();
        assertEquals(80, resumen.getProgreso());
        assertEquals(Urgencia.alta, resumen.getUrgencia());

        assertEquals(4, lineaPresupuestoRepository.countByPresupuestoId(presupuesto.getId()));

        Pieza pieza = piezaRepository.findByReferencia("DS-118").orElseThrow();
        assertEquals(1, pieza.getStock());
    }
}
