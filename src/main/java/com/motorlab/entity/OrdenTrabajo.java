package com.motorlab.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;

import com.motorlab.entity.enums.ConduccionSegura;
import com.motorlab.entity.enums.ContactoPreferido;
import com.motorlab.entity.enums.CuandoOcurre;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.FranjaPreferida;
import com.motorlab.entity.enums.TipoServicio;
import com.motorlab.entity.enums.Urgencia;

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

@Entity
@Table(name = "ordenes_trabajo")
@DynamicUpdate
public class OrdenTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mecanico_id")
    private Usuario mecanico;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false, length = 20)
    private TipoServicio tipoServicio;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sintoma;

    @Enumerated(EnumType.STRING)
    @Column(name = "cuando_ocurre", length = 15)
    private CuandoOcurre cuandoOcurre;

    @Enumerated(EnumType.STRING)
    @Column(name = "conduccion_segura", nullable = false, length = 5)
    private ConduccionSegura conduccionSegura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Urgencia urgencia = Urgencia.media;

    @Column(name = "fecha_preferida", nullable = false)
    private LocalDate fechaPreferida;

    @Enumerated(EnumType.STRING)
    @Column(name = "franja_preferida", nullable = false, length = 6)
    private FranjaPreferida franjaPreferida;

    @Enumerated(EnumType.STRING)
    @Column(name = "contacto_preferido", nullable = false, length = 8)
    private ContactoPreferido contactoPreferido = ContactoPreferido.telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoOrden estado = EstadoOrden.solicitud_recibida;

    @Column(length = 20)
    private String elevador;

    @Column(name = "fecha_entrada")
    private LocalDateTime fechaEntrada;

    @Column(name = "fecha_estimada")
    private LocalDateTime fechaEstimada;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private LocalDateTime actualizadoEn;

    public OrdenTrabajo() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Usuario getMecanico() {
        return mecanico;
    }

    public void setMecanico(Usuario mecanico) {
        this.mecanico = mecanico;
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

    public CuandoOcurre getCuandoOcurre() {
        return cuandoOcurre;
    }

    public void setCuandoOcurre(CuandoOcurre cuandoOcurre) {
        this.cuandoOcurre = cuandoOcurre;
    }

    public ConduccionSegura getConduccionSegura() {
        return conduccionSegura;
    }

    public void setConduccionSegura(ConduccionSegura conduccionSegura) {
        this.conduccionSegura = conduccionSegura;
    }

    public Urgencia getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(Urgencia urgencia) {
        this.urgencia = urgencia;
    }

    public LocalDate getFechaPreferida() {
        return fechaPreferida;
    }

    public void setFechaPreferida(LocalDate fechaPreferida) {
        this.fechaPreferida = fechaPreferida;
    }

    public FranjaPreferida getFranjaPreferida() {
        return franjaPreferida;
    }

    public void setFranjaPreferida(FranjaPreferida franjaPreferida) {
        this.franjaPreferida = franjaPreferida;
    }

    public ContactoPreferido getContactoPreferido() {
        return contactoPreferido;
    }

    public void setContactoPreferido(ContactoPreferido contactoPreferido) {
        this.contactoPreferido = contactoPreferido;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
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

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}
