package com.motorlab.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.EstadoPresupuesto;
import com.motorlab.entity.enums.TipoServicio;
import com.motorlab.entity.enums.Urgencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Immutable
@Table(name = "v_ordenes_resumen")
public class OrdenResumen {

    @Id
    @Column(name = "orden_id")
    private Integer ordenId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoOrden estado;

    @Column(nullable = false)
    private int progreso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Urgencia urgencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false, length = 20)
    private TipoServicio tipoServicio;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sintoma;

    @Column(length = 20)
    private String elevador;

    @Column(name = "fecha_entrada")
    private LocalDateTime fechaEntrada;

    @Column(name = "fecha_estimada")
    private LocalDateTime fechaEstimada;

    @Column(name = "vehiculo_id", nullable = false)
    private Integer vehiculoId;

    @Column(nullable = false, length = 10)
    private String matricula;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Column(name = "cliente_id", nullable = false)
    private Integer clienteId;

    @Column(name = "cliente_nombre", nullable = false, length = 100)
    private String clienteNombre;

    @Column(name = "mecanico_id")
    private Integer mecanicoId;

    @Column(name = "mecanico_nombre", length = 100)
    private String mecanicoNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "presupuesto_estado", length = 10)
    private EstadoPresupuesto presupuestoEstado;

    @Column(name = "presupuesto_total", precision = 10, scale = 2)
    private BigDecimal presupuestoTotal;

    public OrdenResumen() {
    }

    public Integer getOrdenId() {
        return ordenId;
    }

    public void setOrdenId(Integer ordenId) {
        this.ordenId = ordenId;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }

    public int getProgreso() {
        return progreso;
    }

    public void setProgreso(int progreso) {
        this.progreso = progreso;
    }

    public Urgencia getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(Urgencia urgencia) {
        this.urgencia = urgencia;
    }

    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(TipoServicio tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public String getSintoma() {
        return sintoma;
    }

    public void setSintoma(String sintoma) {
        this.sintoma = sintoma;
    }

    public String getElevador() {
        return elevador;
    }

    public void setElevador(String elevador) {
        this.elevador = elevador;
    }

    public LocalDateTime getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDateTime fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public LocalDateTime getFechaEstimada() {
        return fechaEstimada;
    }

    public void setFechaEstimada(LocalDateTime fechaEstimada) {
        this.fechaEstimada = fechaEstimada;
    }

    public Integer getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Integer vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public Integer getMecanicoId() {
        return mecanicoId;
    }

    public void setMecanicoId(Integer mecanicoId) {
        this.mecanicoId = mecanicoId;
    }

    public String getMecanicoNombre() {
        return mecanicoNombre;
    }

    public void setMecanicoNombre(String mecanicoNombre) {
        this.mecanicoNombre = mecanicoNombre;
    }

    public EstadoPresupuesto getPresupuestoEstado() {
        return presupuestoEstado;
    }

    public void setPresupuestoEstado(EstadoPresupuesto presupuestoEstado) {
        this.presupuestoEstado = presupuestoEstado;
    }

    public BigDecimal getPresupuestoTotal() {
        return presupuestoTotal;
    }

    public void setPresupuestoTotal(BigDecimal presupuestoTotal) {
        this.presupuestoTotal = presupuestoTotal;
    }
}
