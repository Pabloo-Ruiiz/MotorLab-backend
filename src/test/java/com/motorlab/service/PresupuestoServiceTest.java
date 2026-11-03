package com.motorlab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.motorlab.dto.orden.OrdenCreateRequest;
import com.motorlab.dto.presupuesto.LineaCreadaResponse;
import com.motorlab.dto.presupuesto.LineaRequest;
import com.motorlab.dto.presupuesto.PresupuestoResponse;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.ConduccionSegura;
import com.motorlab.entity.enums.ContactoPreferido;
import com.motorlab.entity.enums.CuandoOcurre;
import com.motorlab.entity.enums.FranjaPreferida;
import com.motorlab.entity.enums.Rol;
import com.motorlab.entity.enums.TipoLinea;
import com.motorlab.entity.enums.TipoServicio;
import com.motorlab.entity.enums.Urgencia;
import com.motorlab.exception.PresupuestoNoEditableException;
import com.motorlab.exception.RecursoNoEncontradoException;
import com.motorlab.exception.SolicitudInvalidaException;
import com.motorlab.exception.TransicionNoPermitidaException;
import com.motorlab.repository.UsuarioRepository;
import com.motorlab.security.SecurityUtils;
import com.motorlab.security.UsuarioPrincipal;
import com.motorlab.support.IntegracionBase;

@TestPropertySource(properties = "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-caracteres")
class PresupuestoServiceTest extends IntegracionBase {

    private static final Integer VEHICULO_CLIENTE_EJEMPLO = 1;

    private final PresupuestoService presupuestoService;
    private final OrdenService ordenService;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    PresupuestoServiceTest(
            PresupuestoService presupuestoService,
            OrdenService ordenService,
            UsuarioRepository usuarioRepository) {
        this.presupuestoService = presupuestoService;
        this.ordenService = ordenService;
        this.usuarioRepository = usuarioRepository;
    }

    @AfterEach
    void limpiarSeguridad() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void reproduceDemostracionCompletaDePresupuesto() {
        Usuario cliente = usuarioRepository.findByEmail("laura@cliente.example").orElseThrow();
        Usuario administrador = usuarioRepository.findByEmail("admin@taller.example").orElseThrow();
        Usuario otroCliente = crearOtroCliente();

        cambiarUsuario(cliente);
        Integer ordenId = ordenService.crear(solicitudDeFrenos()).id();

        cambiarUsuario(administrador);
        PresupuestoResponse presupuesto = presupuestoService.crear(ordenId);

        LineaCreadaResponse pastillas = presupuestoService.anadirLinea(
                presupuesto.id(), new LineaRequest(TipoLinea.pieza, 1, null, bd("1"), null));
        assertEquals(0, pastillas.totales().subtotal().compareTo(bd("86.00")));

        LineaCreadaResponse discos = presupuestoService.anadirLinea(
                presupuesto.id(), new LineaRequest(TipoLinea.pieza, 2, null, bd("2"), null));
        assertEquals("DS-118", discos.avisoStock().referencia());
        assertEquals(0, discos.avisoStock().necesarias().compareTo(bd("2")));

        presupuestoService.anadirLinea(
                presupuesto.id(), new LineaRequest(TipoLinea.pieza, 3, null, bd("1"), null));
        LineaCreadaResponse manoDeObra = presupuestoService.anadirLinea(
                presupuesto.id(),
                new LineaRequest(TipoLinea.mano_obra, null, "Mano de obra", bd("1.5"), bd("28")));

        assertEquals(0, manoDeObra.totales().subtotal().compareTo(bd("212.00")));
        assertEquals(0, manoDeObra.totales().iva().compareTo(bd("44.52")));
        assertEquals(0, manoDeObra.totales().total().compareTo(bd("256.52")));

        PresupuestoResponse conCuatroLineas = presupuestoService.obtenerPorOrden(ordenId);
        assertEquals(4, conCuatroLineas.lineas().size());

        presupuestoService.eliminarLinea(presupuesto.id(), manoDeObra.linea().id());
        PresupuestoResponse sinManoDeObra = presupuestoService.obtenerPorOrden(ordenId);
        assertEquals(0, sinManoDeObra.subtotal().compareTo(bd("170.00")));
        assertEquals(0, sinManoDeObra.iva().compareTo(bd("35.70")));
        assertEquals(0, sinManoDeObra.total().compareTo(bd("205.70")));

        Integer ordenVaciaId = crearOrdenComoCliente(cliente);
        PresupuestoResponse presupuestoVacio = presupuestoService.crear(ordenVaciaId);
        assertThrows(
                SolicitudInvalidaException.class,
                () -> presupuestoService.enviar(presupuestoVacio.id()));

        presupuestoService.enviar(presupuesto.id());
        assertThrows(
                PresupuestoNoEditableException.class,
                () -> presupuestoService.actualizar(
                        presupuesto.id(),
                        new com.motorlab.dto.presupuesto.PresupuestoUpdateRequest(bd("2"), 24)));

        cambiarUsuario(otroCliente);
        assertThrows(
                RecursoNoEncontradoException.class,
                () -> presupuestoService.autorizar(presupuesto.id()));

        cambiarUsuario(cliente);
        PresupuestoResponse autorizado = presupuestoService.autorizar(presupuesto.id());
        assertEquals(com.motorlab.entity.enums.EstadoPresupuesto.autorizado, autorizado.estado());
        assertThrows(
                TransicionNoPermitidaException.class,
                () -> presupuestoService.autorizar(presupuesto.id()));
    }

    private Integer crearOrdenComoCliente(Usuario cliente) {
        cambiarUsuario(cliente);
        Integer ordenId = ordenService.crear(solicitudDeFrenos()).id();
        cambiarUsuario(usuarioRepository.findByEmail("admin@taller.example").orElseThrow());
        return ordenId;
    }

    private Usuario crearOtroCliente() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Otro Cliente");
        usuario.setEmail("otro-cliente-presupuesto@example.com");
        usuario.setTelefono("600000099");
        usuario.setPasswordHash("hash-de-pruebas");
        usuario.setRol(Rol.cliente);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    private OrdenCreateRequest solicitudDeFrenos() {
        return new OrdenCreateRequest(
                VEHICULO_CLIENTE_EJEMPLO,
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
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        UsuarioPrincipal.desde(usuario),
                        null,
                        UsuarioPrincipal.desde(usuario).getAuthorities()));
    }

    private BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }
}
