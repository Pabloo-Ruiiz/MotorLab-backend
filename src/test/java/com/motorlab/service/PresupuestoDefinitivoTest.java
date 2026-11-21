package com.motorlab.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.motorlab.dto.orden.AsignacionRequest;
import com.motorlab.dto.orden.CambioEstadoRequest;
import com.motorlab.dto.orden.OrdenCreateRequest;
import com.motorlab.dto.presupuesto.LineaRequest;
import com.motorlab.dto.presupuesto.PresupuestoDefinitivoResponse;
import com.motorlab.dto.presupuesto.PresupuestoResponse;
import com.motorlab.entity.OrdenTrabajo;
import com.motorlab.entity.Pieza;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.ConduccionSegura;
import com.motorlab.entity.enums.ContactoPreferido;
import com.motorlab.entity.enums.CuandoOcurre;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.FranjaPreferida;
import com.motorlab.entity.enums.Rol;
import com.motorlab.entity.enums.TipoLinea;
import com.motorlab.entity.enums.TipoServicio;
import com.motorlab.entity.enums.Urgencia;
import com.motorlab.exception.RecursoNoEncontradoException;
import com.motorlab.exception.TransicionNoPermitidaException;
import com.motorlab.repository.OrdenTrabajoRepository;
import com.motorlab.repository.PiezaRepository;
import com.motorlab.repository.PresupuestoDefinitivoRepository;
import com.motorlab.repository.UsuarioRepository;
import com.motorlab.security.UsuarioPrincipal;
import com.motorlab.support.IntegracionBase;

@TestPropertySource(properties = "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-caracteres")
class PresupuestoDefinitivoTest extends IntegracionBase {

    private static final Integer VEHICULO_CLIENTE = 1;
    private static final Integer PIEZA_PASTILLAS = 1;

    @Autowired
    private OrdenService ordenService;

    @Autowired
    private PresupuestoService presupuestoService;

    @Autowired
    private PresupuestoDefinitivoService definitivoService;

    @Autowired
    private PresupuestoDefinitivoRepository definitivoRepository;

    @Autowired
    private OrdenTrabajoRepository ordenRepository;

    @Autowired
    private PiezaRepository piezaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void unaOrdenRecienCreadaNoTienePresupuestoDefinitivo() {
        cambiarUsuario(usuario("laura@cliente.example"));
        Integer ordenId = ordenService.crear(solicitud()).id();

        assertNoDisponible(ordenId);
    }

    @Test
    void unaOrdenEnReparacionNoTienePresupuestoDefinitivo() {
        Integer ordenId = llevarHasta(EstadoOrden.arreglando);

        cambiarUsuario(usuario("laura@cliente.example"));
        assertNoDisponible(ordenId);
        cambiarUsuario(usuario("admin@taller.example"));
        assertNoDisponible(ordenId);
    }

    @Test
    void alFinalizarLaReparacionSeEmiteConLosImportesDeLaOrden() {
        Integer ordenId = llevarHasta(EstadoOrden.listo);

        cambiarUsuario(usuario("laura@cliente.example"));
        PresupuestoResponse presupuesto = presupuestoService.obtenerPorOrden(ordenId);
        PresupuestoDefinitivoResponse definitivo = definitivoService.consultar(ordenId);

        assertEquals(ordenId, definitivo.ordenId());
        assertEquals(presupuesto.id(), definitivo.presupuestoId());
        assertTrue(definitivo.numero().matches("PD-\\d{4}-\\d{5}"));
        assertEquals("4821KLB", definitivo.vehiculo().matricula());
        assertEquals("laura@cliente.example", definitivo.cliente().email());
        assertEquals(presupuesto.lineas().size(), definitivo.lineas().size());
        assertIgual(presupuesto.subtotal(), definitivo.subtotal());
        assertIgual(presupuesto.iva(), definitivo.iva());
        assertIgual(presupuesto.total(), definitivo.total());
        assertIgual(new BigDecimal("86.00"), definitivo.subtotalPiezas());
        assertIgual(new BigDecimal("42.00"), definitivo.subtotalManoObra());
        assertIgual(definitivo.subtotal(), definitivo.subtotalPiezas().add(definitivo.subtotalManoObra()));
    }

    @Test
    void finalizarOtraVezNoDuplicaElPresupuestoNiCambiaSusImportes() {
        Integer ordenId = llevarHasta(EstadoOrden.listo);
        OrdenTrabajo orden = ordenRepository.findById(ordenId).orElseThrow();
        Integer emitido = definitivoRepository.findByOrdenId(ordenId).orElseThrow().getId();

        // El catálogo cambia después de emitir: el documento conserva el precio de la reparación
        Pieza pastillas = piezaRepository.findById(PIEZA_PASTILLAS).orElseThrow();
        pastillas.setPrecio(new BigDecimal("199.99"));
        piezaRepository.saveAndFlush(pastillas);

        assertEquals(emitido, definitivoService.generar(orden).getId());
        assertThrows(TransicionNoPermitidaException.class,
                () -> cambiarEstado(ordenId, EstadoOrden.listo, "Otra vez"));
        assertEquals(1, definitivoRepository.findAll().stream()
                .filter(definitivo -> definitivo.getOrden().getId().equals(ordenId))
                .count());

        cambiarUsuario(usuario("laura@cliente.example"));
        assertIgual(new BigDecimal("86.00"), definitivoService.consultar(ordenId).lineas().stream()
                .filter(linea -> linea.tipo() == TipoLinea.pieza)
                .findFirst().orElseThrow().precioUnitario());
    }

    @Test
    void elClienteDescargaUnPdfConLosMismosImportes() throws Exception {
        Integer ordenId = llevarHasta(EstadoOrden.listo);

        cambiarUsuario(usuario("laura@cliente.example"));
        PresupuestoDefinitivoResponse definitivo = definitivoService.consultar(ordenId);
        PresupuestoDefinitivoService.DocumentoPdf pdf = definitivoService.descargar(ordenId);

        assertEquals("presupuesto-" + definitivo.numero() + ".pdf", pdf.nombreArchivo());
        assertArrayEquals("%PDF".getBytes(StandardCharsets.US_ASCII), Arrays.copyOf(pdf.contenido(), 4));
        String texto = new PdfTextExtractor(new PdfReader(pdf.contenido())).getTextFromPage(1);
        assertTrue(texto.contains("Presupuesto de reparación"));
        assertTrue(texto.contains(definitivo.numero()));
        assertTrue(texto.contains("#" + ordenId));
        assertTrue(texto.contains("4821 KLB"));
        for (BigDecimal importe : new BigDecimal[] {
                definitivo.subtotalManoObra(), definitivo.subtotalPiezas(),
                definitivo.subtotal(), definitivo.iva(), definitivo.total()}) {
            assertTrue(texto.contains(euros(importe)), "Falta el importe " + euros(importe) + " en el PDF");
        }
    }

    @Test
    void otroClienteNoPuedeVerNiDescargarElPresupuesto() {
        Integer ordenId = llevarHasta(EstadoOrden.listo);
        Usuario otroCliente = new Usuario();
        otroCliente.setNombre("Otro cliente");
        otroCliente.setEmail("otro.cliente@cliente.example");
        otroCliente.setTelefono("600000099");
        otroCliente.setPasswordHash("sin-uso");
        otroCliente.setRol(Rol.cliente);
        cambiarUsuario(usuarioRepository.saveAndFlush(otroCliente));

        assertThrows(RecursoNoEncontradoException.class, () -> definitivoService.consultar(ordenId));
        assertThrows(RecursoNoEncontradoException.class, () -> definitivoService.descargar(ordenId));
    }

    private Integer llevarHasta(EstadoOrden destino) {
        Usuario cliente = usuario("laura@cliente.example");
        Usuario administrador = usuario("admin@taller.example");
        Usuario mecanico = usuario("javi@taller.example");

        cambiarUsuario(cliente);
        Integer ordenId = ordenService.crear(solicitud()).id();

        cambiarUsuario(administrador);
        PresupuestoResponse presupuesto = presupuestoService.crear(ordenId);
        presupuestoService.anadirLinea(presupuesto.id(),
                new LineaRequest(TipoLinea.pieza, PIEZA_PASTILLAS, null, new BigDecimal("1"), null));
        presupuestoService.anadirLinea(presupuesto.id(),
                new LineaRequest(TipoLinea.mano_obra, null, "Cambio de pastillas", new BigDecimal("1.5"),
                        new BigDecimal("28")));
        ordenService.asignar(ordenId,
                new AsignacionRequest(mecanico.getId(), "Elevador 1", LocalDateTime.now().plusHours(3)));
        presupuestoService.enviar(presupuesto.id());

        cambiarUsuario(cliente);
        presupuestoService.autorizar(presupuesto.id());
        assertFalse(definitivoRepository.existsByOrdenId(ordenId));

        cambiarUsuario(administrador);
        cambiarEstado(ordenId, EstadoOrden.recibido, "Vehículo recibido");
        cambiarUsuario(mecanico);
        for (EstadoOrden estado : new EstadoOrden[] {
                EstadoOrden.en_preparacion, EstadoOrden.arreglando, EstadoOrden.listo}) {
            cambiarEstado(ordenId, estado, "Avance de la reparación");
            if (estado == destino) {
                break;
            }
            assertFalse(definitivoRepository.existsByOrdenId(ordenId));
        }
        assertEquals(destino == EstadoOrden.listo, definitivoRepository.existsByOrdenId(ordenId));
        return ordenId;
    }

    private void assertNoDisponible(Integer ordenId) {
        assertThrows(RecursoNoEncontradoException.class, () -> definitivoService.consultar(ordenId));
        assertThrows(RecursoNoEncontradoException.class, () -> definitivoService.descargar(ordenId));
        assertFalse(definitivoRepository.existsByOrdenId(ordenId));
    }

    private void cambiarEstado(Integer ordenId, EstadoOrden estado, String descripcion) {
        ordenService.cambiarEstado(ordenId, new CambioEstadoRequest(estado, descripcion));
    }

    private static void assertIgual(BigDecimal esperado, BigDecimal real) {
        assertEquals(0, esperado.compareTo(real), () -> "Esperado " + esperado + " y es " + real);
    }

    private static String euros(BigDecimal importe) {
        return new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-ES")))
                .format(importe) + " €";
    }

    private Usuario usuario(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow();
    }

    private static OrdenCreateRequest solicitud() {
        return new OrdenCreateRequest(
                VEHICULO_CLIENTE,
                TipoServicio.frenos,
                "Hace un ruido metálico al frenar en bajada",
                CuandoOcurre.al_frenar,
                ConduccionSegura.si,
                Urgencia.media,
                LocalDate.now(),
                FranjaPreferida.manana,
                ContactoPreferido.telefono,
                true);
    }

    private static void cambiarUsuario(Usuario usuario) {
        UsuarioPrincipal principal = UsuarioPrincipal.desde(usuario);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
