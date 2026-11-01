package com.motorlab.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.motorlab.entity.OrdenTrabajo;
import com.motorlab.entity.enums.EstadoOrden;

public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Integer> {

    List<OrdenTrabajo> findByVehiculoIdOrderByIdDesc(Integer vehiculoId);

    boolean existsByVehiculoIdAndEstadoNotIn(Integer vehiculoId, Collection<EstadoOrden> estados);

    @Query("select o.estado, count(o) from OrdenTrabajo o group by o.estado")
    List<Object[]> contarPorEstado();
}
