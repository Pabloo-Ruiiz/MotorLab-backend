package com.motorlab.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.motorlab.entity.OrdenTrabajo;
import com.motorlab.entity.Presupuesto;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.Vehiculo;
import com.motorlab.entity.enums.EstadoPago;
import com.motorlab.entity.enums.EstadoPresupuesto;
import com.motorlab.entity.enums.MetodoPago;
import com.motorlab.entity.enums.Rol;
import com.motorlab.entity.enums.TipoNotificacion;
import com.motorlab.exception.ConflictoException;
import com.motorlab.exception.PagosNoDisponiblesException;
import com.motorlab.exception.RecursoNoEncontradoException;
import com.motorlab.exception.TransicionNoPermitidaException;
import com.motorlab.repository.PresupuestoRepository;
import com.motorlab.security.UsuarioPrincipal;
import com.motorlab.service.pago.PasarelaPago;
import com.motorlab.service.pago.PasarelaPago.NuevaSesionPago;
import com.motorlab.service.pago.PasarelaPago.PagoCompletado;
import com.motorlab.service.pago.PasarelaPago.SesionPago;

class PagoServiceTest {

    private final PresupuestoRepository repositorio = mock(PresupuestoRepository.class);
    private final NotificacionService notificaciones = mock(NotificacionService.class);
    private final PasarelaPago pasarela = mock(PasarelaPago.class);
    private final PagoService servicio =
            new PagoService(repositorio, notificaciones, pasarela, "http://localhost:4200");

    private Usuario cliente;
    private Presupuesto presupuesto;

    @BeforeEach
    void prepararDatos() {
        cliente = usuario(7, Rol.cliente);
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setUsuario(cliente);
        vehiculo.setMarca("Seat");
        vehiculo.setModelo("León");
        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setId(9);
        orden.setVehiculo(vehiculo);

        presupuesto = new Presupuesto();
        presupuesto.setId(5);
        presupuesto.setOrden(orden);
        presupuesto.setEstado(EstadoPresupuesto.autorizado);
        presupuesto.setTotal(new BigDecimal("272.13"));

        when(repositorio.findByIdForUpdate(5)).thenReturn(Optional.of(presupuesto));
        when(pasarela.configurada()).thenReturn(true);
        when(pasarela.crearSesion(any())).thenReturn(new SesionPago("cs_test_1", "https://checkout.stripe.com/c/pay/x"));
        autenticar(cliente);
    }

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void iniciarCreaLaSesionConElImporteDelPresupuestoEnCentimos() {
        var respuesta = servicio.iniciar(5);

        ArgumentCaptor<NuevaSesionPago> datos = ArgumentCaptor.forClass(NuevaSesionPago.class);
        verify(pasarela).crearSesion(datos.capture());
        assertThat(datos.getValue().importeCentimos()).isEqualTo(27213);
        assertThat(datos.getValue().concepto()).isEqualTo("Presupuesto P-5 · Seat León");
        assertThat(datos.getValue().urlExito())
                .isEqualTo("http://localhost:4200/cliente/ordenes/9/presupuesto?pago=ok");
        assertThat(respuesta.url()).isEqualTo("https://checkout.stripe.com/c/pay/x");
        assertThat(presupuesto.getStripeSesionId()).isEqualTo("cs_test_1");
    }

    @Test
    void noSePuedePagarUnPresupuestoSinAutorizar() {
        presupuesto.setEstado(EstadoPresupuesto.enviado);

        assertThatThrownBy(() -> servicio.iniciar(5)).isInstanceOf(TransicionNoPermitidaException.class);
        verify(pasarela, never()).crearSesion(any());
    }

    @Test
    void noSePuedePagarDosVeces() {
        presupuesto.setPagadoEn(LocalDateTime.now());

        assertThatThrownBy(() -> servicio.iniciar(5)).isInstanceOf(ConflictoException.class);
    }

    @Test
    void otroClienteNoVeElPresupuesto() {
        autenticar(usuario(8, Rol.cliente));

        assertThatThrownBy(() -> servicio.iniciar(5)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void sinStripeConfiguradoElPagoNoEstaDisponible() {
        when(pasarela.configurada()).thenReturn(false);

        assertThatThrownBy(() -> servicio.iniciar(5)).isInstanceOf(PagosNoDisponiblesException.class);
    }

    @Test
    void confirmarMarcaElPresupuestoPagadoYAvisaAlTaller() {
        servicio.confirmar(new PagoCompletado("cs_test_1", 5, 27213, "eur"));

        assertThat(presupuesto.getPagadoEn()).isNotNull();
        assertThat(presupuesto.getMetodoPago()).isEqualTo(MetodoPago.tarjeta);
        assertThat(presupuesto.getEstadoPago()).isEqualTo(EstadoPago.pagado_tarjeta);
        verify(notificaciones).notificarAdmins(
                eq(presupuesto.getOrden()), eq(TipoNotificacion.presupuesto_respondido), anyString());
    }

    @Test
    void pagarConTarjetaTrasElegirEfectivoSustituyeElEfectivo() {
        servicio.elegirEfectivo(5);

        servicio.confirmar(new PagoCompletado("cs_test_1", 5, 27213, "eur"));

        assertThat(presupuesto.getMetodoPago()).isEqualTo(MetodoPago.tarjeta);
        assertThat(presupuesto.getEstadoPago()).isEqualTo(EstadoPago.pagado_tarjeta);
    }

    @Test
    void elegirEfectivoDejaElPresupuestoPendienteDeCobroYAvisaAlTaller() {
        servicio.elegirEfectivo(5);

        assertThat(presupuesto.getMetodoPago()).isEqualTo(MetodoPago.efectivo);
        assertThat(presupuesto.getEstadoPago()).isEqualTo(EstadoPago.pendiente_efectivo);
        assertThat(presupuesto.getPagadoEn()).isNull();
        verify(notificaciones).notificarAdmins(
                eq(presupuesto.getOrden()), eq(TipoNotificacion.presupuesto_respondido), anyString());
    }

    @Test
    void elegirEfectivoDosVecesSoloAvisaUnaVez() {
        servicio.elegirEfectivo(5);
        servicio.elegirEfectivo(5);

        verify(notificaciones, times(1)).notificarAdmins(any(), any(), anyString());
    }

    @Test
    void noSePuedeElegirEfectivoSinAutorizarNiYaPagado() {
        presupuesto.setEstado(EstadoPresupuesto.enviado);
        assertThatThrownBy(() -> servicio.elegirEfectivo(5)).isInstanceOf(TransicionNoPermitidaException.class);

        presupuesto.setEstado(EstadoPresupuesto.autorizado);
        presupuesto.setPagadoEn(LocalDateTime.now());
        assertThatThrownBy(() -> servicio.elegirEfectivo(5)).isInstanceOf(ConflictoException.class);
    }

    @Test
    void otroClienteNoPuedeElegirEfectivo() {
        autenticar(usuario(8, Rol.cliente));

        assertThatThrownBy(() -> servicio.elegirEfectivo(5)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cobrarEfectivoMarcaElPresupuestoPagadoYAvisaAlCliente() {
        autenticar(usuario(1, Rol.admin));
        presupuesto.setMetodoPago(MetodoPago.efectivo);
        presupuesto.setEstadoPago(EstadoPago.pendiente_efectivo);

        servicio.cobrarEfectivo(5);

        assertThat(presupuesto.getEstadoPago()).isEqualTo(EstadoPago.pagado_efectivo);
        assertThat(presupuesto.getMetodoPago()).isEqualTo(MetodoPago.efectivo);
        assertThat(presupuesto.getPagadoEn()).isNotNull();
        verify(notificaciones).notificarCliente(
                eq(presupuesto.getOrden()), eq(TipoNotificacion.presupuesto_respondido), anyString());
    }

    @Test
    void noSePuedeCobrarDosVeces() {
        autenticar(usuario(1, Rol.admin));
        servicio.cobrarEfectivo(5);

        assertThatThrownBy(() -> servicio.cobrarEfectivo(5)).isInstanceOf(ConflictoException.class);
    }

    @Test
    void unAvisoRepetidoNoVuelveANotificar() {
        PagoCompletado pago = new PagoCompletado("cs_test_1", 5, 27213, "eur");

        servicio.confirmar(pago);
        servicio.confirmar(pago);

        verify(notificaciones, times(1)).notificarAdmins(any(), any(), anyString());
    }

    @Test
    void unImporteDistintoNoMarcaElPresupuestoComoPagado() {
        servicio.confirmar(new PagoCompletado("cs_test_1", 5, 100, "eur"));

        assertThat(presupuesto.getPagadoEn()).isNull();
        verify(notificaciones, never()).notificarAdmins(any(), any(), anyString());
    }

    private static Usuario usuario(int id, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre("Usuario " + id);
        usuario.setEmail("usuario" + id + "@cliente.example");
        usuario.setPasswordHash("x");
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuario;
    }

    private static void autenticar(Usuario usuario) {
        UsuarioPrincipal principal = UsuarioPrincipal.desde(usuario);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
