package com.motorlab.entity;

import java.math.BigDecimal;

import com.motorlab.entity.enums.TipoLinea;

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
import jakarta.persistence.Table;

// Copia de una línea del presupuesto en el momento de finalizar la reparación
@Entity
@Table(name = "lineas_presupuesto_definitivo")
public class LineaPresupuestoDefinitivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_definitivo_id", nullable = false)
    private PresupuestoDefinitivo presupuestoDefinitivo;

    @Column(nullable = false)
    private Integer posicion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoLinea tipo;

    @Column(length = 30)
    private String referencia;

    @Column(nullable = false, length = 150)
    private String concepto;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    public Integer getId() {
        return id;
    }

    public PresupuestoDefinitivo getPresupuestoDefinitivo() {
        return presupuestoDefinitivo;
    }

    public void setPresupuestoDefinitivo(PresupuestoDefinitivo presupuestoDefinitivo) {
        this.presupuestoDefinitivo = presupuestoDefinitivo;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }

    public TipoLinea getTipo() {
        return tipo;
    }

    public void setTipo(TipoLinea tipo) {
        this.tipo = tipo;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }
}
