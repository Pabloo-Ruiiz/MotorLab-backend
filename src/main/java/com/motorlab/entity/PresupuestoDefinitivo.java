package com.motorlab.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.motorlab.entity.enums.TipoServicio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * Presupuesto que se emite al finalizar la reparación. Guarda una copia de los datos y de los
 * importes: no cambia aunque después varíen los precios del catálogo, el cliente o el vehículo.
 */
@Entity
@Table(name = "presupuestos_definitivos")
public class PresupuestoDefinitivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false, unique = true)
    private OrdenTrabajo orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_id", nullable = false)
    private Presupuesto presupuesto;

    @Column(name = "emitido_en", nullable = false)
    private LocalDateTime emitidoEn;

    @Column(name = "cliente_nombre", nullable = false, length = 100)
    private String clienteNombre;

    @Column(name = "cliente_email", nullable = false, length = 150)
    private String clienteEmail;

    @Column(name = "cliente_telefono", nullable = false, length = 20)
    private String clienteTelefono;

    @Column(name = "vehiculo_matricula", nullable = false, length = 10)
    private String vehiculoMatricula;

    @Column(name = "vehiculo_marca", nullable = false, length = 50)
    private String vehiculoMarca;

    @Column(name = "vehiculo_modelo", nullable = false, length = 80)
    private String vehiculoModelo;

    @Column(name = "vehiculo_version", length = 80)
    private String vehiculoVersion;

    @Column(name = "vehiculo_anio", nullable = false)
    private Integer vehiculoAnio;

    @Column(name = "vehiculo_kilometros", nullable = false)
    private Integer vehiculoKilometros;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false, length = 20)
    private TipoServicio tipoServicio;

    @Column(name = "subtotal_mano_obra", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalManoObra;

    @Column(name = "subtotal_piezas", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalPiezas;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "iva_porcentaje", nullable = false, precision = 4, scale = 2)
    private BigDecimal ivaPorcentaje;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal iva;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "presupuestoDefinitivo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicion ASC")
    private List<LineaPresupuestoDefinitivo> lineas = new ArrayList<>();

    public void anadirLinea(LineaPresupuestoDefinitivo linea) {
        linea.setPresupuestoDefinitivo(this);
        linea.setPosicion(lineas.size() + 1);
        lineas.add(linea);
    }

    // PD-2026-00012: año de emisión y número correlativo del documento
    public String getNumero() {
        return id == null || emitidoEn == null ? null : "PD-%d-%05d".formatted(emitidoEn.getYear(), id);
    }

    public Integer getId() {
        return id;
    }

    public OrdenTrabajo getOrden() {
        return orden;
    }

    public void setOrden(OrdenTrabajo orden) {
        this.orden = orden;
    }

    public Presupuesto getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(Presupuesto presupuesto) {
        this.presupuesto = presupuesto;
    }

    public LocalDateTime getEmitidoEn() {
        return emitidoEn;
    }

    public void setEmitidoEn(LocalDateTime emitidoEn) {
        this.emitidoEn = emitidoEn;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public String getVehiculoMatricula() {
        return vehiculoMatricula;
    }

    public void setVehiculoMatricula(String vehiculoMatricula) {
        this.vehiculoMatricula = vehiculoMatricula;
    }

    public String getVehiculoMarca() {
        return vehiculoMarca;
    }

    public void setVehiculoMarca(String vehiculoMarca) {
        this.vehiculoMarca = vehiculoMarca;
    }

    public String getVehiculoModelo() {
        return vehiculoModelo;
    }

    public void setVehiculoModelo(String vehiculoModelo) {
        this.vehiculoModelo = vehiculoModelo;
    }

    public String getVehiculoVersion() {
        return vehiculoVersion;
    }

    public void setVehiculoVersion(String vehiculoVersion) {
        this.vehiculoVersion = vehiculoVersion;
    }

    public Integer getVehiculoAnio() {
        return vehiculoAnio;
    }

    public void setVehiculoAnio(Integer vehiculoAnio) {
        this.vehiculoAnio = vehiculoAnio;
    }

    public Integer getVehiculoKilometros() {
        return vehiculoKilometros;
    }

    public void setVehiculoKilometros(Integer vehiculoKilometros) {
        this.vehiculoKilometros = vehiculoKilometros;
    }

    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(TipoServicio tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public BigDecimal getSubtotalManoObra() {
        return subtotalManoObra;
    }

    public void setSubtotalManoObra(BigDecimal subtotalManoObra) {
        this.subtotalManoObra = subtotalManoObra;
    }

    public BigDecimal getSubtotalPiezas() {
        return subtotalPiezas;
    }

    public void setSubtotalPiezas(BigDecimal subtotalPiezas) {
        this.subtotalPiezas = subtotalPiezas;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIvaPorcentaje() {
        return ivaPorcentaje;
    }

    public void setIvaPorcentaje(BigDecimal ivaPorcentaje) {
        this.ivaPorcentaje = ivaPorcentaje;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<LineaPresupuestoDefinitivo> getLineas() {
        return lineas;
    }
}
