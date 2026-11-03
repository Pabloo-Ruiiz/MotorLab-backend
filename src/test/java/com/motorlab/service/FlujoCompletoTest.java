package com.motorlab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.motorlab.dto.notificacion.NotificacionResponse;
import com.motorlab.dto.orden.AsignacionRequest;
import com.motorlab.dto.orden.CambioEstadoRequest;
import com.motorlab.dto.orden.OrdenCreateRequest;
import com.motorlab.dto.orden.OrdenDetalleResponse;
import com.motorlab.dto.orden.SeguimientoResponse;
import com.motorlab.dto.pieza.PiezaResponse;
import com.motorlab.dto.presupuesto.LineaCreadaResponse;
import com.motorlab.dto.presupuesto.LineaRequest;
import com.motorlab.dto.presupuesto.PresupuestoResponse;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.ConduccionSegura;
import com.motorlab.entity.enums.ContactoPreferido;
import com.motorlab.entity.enums.CuandoOcurre;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.FranjaPreferida;
import com.motorlab.entity.enums.Rol;
import com.motorlab.entity.enums.TipoLinea;
import com.motorlab.entity.enums.TipoNotificacion;
import com.motorlab.entity.enums.TipoServicio;
import com.motorlab.entity.enums.Urgencia;
import com.motorlab.exception.ApiException;
import com.motorlab.exception.SinPermisoException;
import com.motorlab.exception.StockInsuficienteException;
import com.motorlab.repository.UsuarioRepository;
import com.motorlab.security.UsuarioPrincipal;
import com.motorlab.support.IntegracionBase;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@TestPropertySource(properties = "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-caracteres")
class FlujoCompletoTest extends IntegracionBase {

    private static final Integer VEHICULO_CLIENTE = 1;
    private static final Integer PIEZA_PASTILLAS = 1;
    private static final Integer PIEZA_DISCOS = 2;
    private static final Integer PIEZA_LIQUIDO = 3;

    private final OrdenService ordenService;
    private final PresupuestoService presupuestoService;
    private final PiezaService piezaService;
    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    FlujoCompletoTest(
            OrdenService ordenService,
            PresupuestoService presupuestoService,
            PiezaService piezaService,
            NotificacionService notificacionService,
            UsuarioRepository usuarioRepository) {
        this.ordenService = ordenService;
        this.presupuestoService = presupuestoService;
        this.piezaService = piezaService;
        this.notificacionService = notificacionService;
        this.usuarioRepository = usuarioRepository;
    }

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reproduceElFlujoCompletoDeUnaReparacion() {
        Usuario cliente = usuario("laura@cliente.example");
        Usuario administrador = usuario("admin@taller.example");
        Usuario mecanico = usuario("javi@taller.example");

        cambiarUsuario(cliente);
        var solicitud = ordenService.crear(solicitudDeFrenos());
        assertEquals(EstadoOrden.solicitud_recibida, solicitud.estado());
        assertEquals(Urgencia.alta, solicitud.urgencia());
        assertEquals(0, solicitud.estado().progreso());

        cambiarUsuario(administrador);
        assertTieneNotificacion(administrador, solicitud.id(), TipoNotificacion.solicitud_nueva);

        PresupuestoResponse presupuesto = presupuestoService.crear(solicitud.id());
        assertEstado(solicitud.id(), EstadoOrden.pendiente_presupuesto, 0);
        anadirLineas(presupuesto.id());
        ordenService.asignar(
                solicitud.id(),
                new AsignacionRequest(
                        mecanico.getId(), "Elevador 2", LocalDateTime.now().plusHours(2)));
        presupuestoService.enviar(presupuesto.id());
        assertEstado(solicitud.id(), EstadoOrden.presupuesto_enviado, 0);

        cambiarUsuario(cliente);
        assertTieneNotificacion(cliente, solicitud.id(), TipoNotificacion.presupuesto_listo);
        presupuestoService.autorizar(presupuesto.id());
        assertEstado(solicitud.id(), EstadoOrden.presupuesto_autorizado, 0);

        cambiarUsuario(administrador);
        OrdenDetalleResponse recibido = cambiarEstado(
                solicitud.id(), EstadoOrden.recibido, "Vehículo recibido en el taller");
        assertEquals(EstadoOrden.recibido, recibido.estado());
        assertEquals(20, recibido.progreso());
        assertNotNull(recibido.fechaEntrada());

        cambiarUsuario(mecanico);
        assertEquals(40, cambiarEstado(
                solicitud.id(), EstadoOrden.en_preparacion, "Diagnóstico y revisión").progreso());
        assertEquals(60, cambiarEstado(
                solicitud.id(), EstadoOrden.esperando_pieza, "Esperando discos").progreso());

        StockInsuficienteException stockInsuficiente = assertThrows(
                StockInsuficienteException.class,
                () -> cambiarEstado(
                        solicitud.id(), EstadoOrden.arreglando, "Inicio de la reparación"));
        assertTrue(stockInsuficiente.getDetalles().stream()
                .anyMatch(detalle -> detalle.campo().equals("pieza_" + PIEZA_DISCOS)));
        assertEstado(solicitud.id(), EstadoOrden.esperando_pieza, 60);

        cambiarUsuario(administrador);
        PiezaResponse discosAjustados = piezaService.ajustarStock(
                PIEZA_DISCOS, 4, "Entrada de pedido");
        assertEquals(5, discosAjustados.stock());

        cambiarUsuario(mecanico);
        assertEquals(80, cambiarEstado(
                solicitud.id(), EstadoOrden.arreglando, "Piezas disponibles").progreso());
        assertEquals(3, piezaService.obtener(PIEZA_PASTILLAS).stock());
        assertEquals(3, piezaService.obtener(PIEZA_DISCOS).stock());
        assertEquals(9, piezaService.obtener(PIEZA_LIQUIDO).stock());

        assertEquals(100, cambiarEstado(
                solicitud.id(), EstadoOrden.listo, "Vehículo listo").progreso());
        cambiarUsuario(cliente);
        assertTieneNotificacion(cliente, solicitud.id(), TipoNotificacion.vehiculo_listo);

        cambiarUsuario(administrador);
        assertEquals(100, cambiarEstado(
                solicitud.id(), EstadoOrden.entregado, "Vehículo entregado").progreso());

        cambiarUsuario(cliente);
        SeguimientoResponse seguimiento = ordenService.seguimiento(solicitud.id());
        assertEquals(EstadoOrden.entregado, seguimiento.estado());
        assertEquals(100, seguimiento.progreso());
        assertEquals(List.of(
                        EstadoOrden.solicitud_recibida,
                        EstadoOrden.pendiente_presupuesto,
                        EstadoOrden.presupuesto_enviado,
                        EstadoOrden.presupuesto_autorizado,
                        EstadoOrden.recibido,
                        EstadoOrden.en_preparacion,
                        EstadoOrden.esperando_pieza,
                        EstadoOrden.arreglando,
                        EstadoOrden.listo,
                        EstadoOrden.entregado),
                seguimiento.eventos().stream().map(evento -> evento.estado()).toList());

        cambiarUsuario(mecanico);
        SinPermisoException sinPermiso = assertThrows(
                SinPermisoException.class,
                () -> cambiarEstado(solicitud.id(), EstadoOrden.recibido, "Transición no permitida"));
        assertEquals(403, sinPermiso.getEstado().value());

        cambiarUsuario(administrador);
        ApiException transicion = assertThrows(
                ApiException.class,
                () -> cambiarEstado(solicitud.id(), EstadoOrden.en_preparacion, "Orden ya entregada"));
        assertEquals(409, transicion.getEstado().value());
    }

    private void anadirLineas(Integer presupuestoId) {
        LineaCreadaResponse pastillas = presupuestoService.anadirLinea(
                presupuestoId,
                new LineaRequest(TipoLinea.pieza, PIEZA_PASTILLAS, null, bd("1"), null));
        assertEquals(0, pastillas.totales().subtotal().compareTo(bd("86.00")));

        LineaCreadaResponse discos = presupuestoService.anadirLinea(
                presupuestoId,
                new LineaRequest(TipoLinea.pieza, PIEZA_DISCOS, null, bd("2"), null));
        assertEquals("DS-118", discos.avisoStock().referencia());

        presupuestoService.anadirLinea(
                presupuestoId,
                new LineaRequest(TipoLinea.pieza, PIEZA_LIQUIDO, null, bd("1"), null));
        presupuestoService.anadirLinea(
                presupuestoId,
                new LineaRequest(TipoLinea.mano_obra, null, "Mano de obra", bd("1.5"), bd("28")));
    }

    private OrdenDetalleResponse cambiarEstado(
            Integer ordenId, EstadoOrden estado, String descripcion) {
        return ordenService.cambiarEstado(
                ordenId, new CambioEstadoRequest(estado, descripcion));
    }

    private void assertEstado(Integer ordenId, EstadoOrden estado, int progreso) {
        OrdenDetalleResponse orden = ordenService.obtener(ordenId);
        assertEquals(estado, orden.estado());
        assertEquals(progreso, orden.progreso());
    }

    private void assertTieneNotificacion(
            Usuario usuario, Integer ordenId, TipoNotificacion tipo) {
        cambiarUsuario(usuario);
        List<NotificacionResponse> notificaciones = notificacionService
                .listar(null, 1, 100)
                .datos();
        assertTrue(notificaciones.stream().anyMatch(notificacion ->
                ordenId.equals(notificacion.ordenId()) && notificacion.tipo() == tipo));
    }

    private Usuario usuario(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow();
    }

    private OrdenCreateRequest solicitudDeFrenos() {
        return new OrdenCreateRequest(
                VEHICULO_CLIENTE,
                TipoServicio.frenos,
                "Hace un ruido raro al frenar en el vehículo",
                CuandoOcurre.al_frenar,
                ConduccionSegura.si,
                Urgencia.media,
                LocalDate.now(),
                FranjaPreferida.manana,
                ContactoPreferido.telefono,
                true);
    }

    private void cambiarUsuario(Usuario usuario) {
        UsuarioPrincipal principal = UsuarioPrincipal.desde(usuario);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));
    }

    private BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }
}
