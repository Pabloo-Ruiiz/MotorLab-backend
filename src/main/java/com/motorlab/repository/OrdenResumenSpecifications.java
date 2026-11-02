package com.motorlab.repository;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.motorlab.entity.OrdenResumen;
import com.motorlab.entity.enums.EstadoOrden;
import com.motorlab.entity.enums.Urgencia;

public final class OrdenResumenSpecifications {

    private OrdenResumenSpecifications() {
    }

    public static Specification<OrdenResumen> delCliente(Integer id) {
        return id == null ? null : (raiz, consulta, criterios) ->
                criterios.equal(raiz.get("clienteId"), id);
    }

    public static Specification<OrdenResumen> delMecanico(Integer id) {
        return id == null ? null : (raiz, consulta, criterios) ->
                criterios.equal(raiz.get("mecanicoId"), id);
    }

    public static Specification<OrdenResumen> delVehiculo(Integer id) {
        return id == null ? null : (raiz, consulta, criterios) ->
                criterios.equal(raiz.get("vehiculoId"), id);
    }

    public static Specification<OrdenResumen> conEstado(EstadoOrden estado) {
        return estado == null ? null : (raiz, consulta, criterios) ->
                criterios.equal(raiz.get("estado"), estado);
    }

    public static Specification<OrdenResumen> conUrgencia(Urgencia urgencia) {
        return urgencia == null ? null : (raiz, consulta, criterios) ->
                criterios.equal(raiz.get("urgencia"), urgencia);
    }

    public static Specification<OrdenResumen> conMecanico(Integer id) {
        return delMecanico(id);
    }

    public static Specification<OrdenResumen> buscar(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        String texto = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
        return (raiz, consulta, criterios) -> criterios.or(
                criterios.like(criterios.lower(raiz.get("matricula")), texto),
                criterios.like(criterios.lower(raiz.get("clienteNombre")), texto));
    }
}
