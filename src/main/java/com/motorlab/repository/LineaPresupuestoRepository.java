package com.motorlab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motorlab.entity.LineaPresupuesto;

public interface LineaPresupuestoRepository extends JpaRepository<LineaPresupuesto, Integer> {

    List<LineaPresupuesto> findByPresupuestoIdOrderByIdAsc(Integer presupuestoId);

    Optional<LineaPresupuesto> findByIdAndPresupuestoId(Integer id, Integer presupuestoId);

    long countByPresupuestoId(Integer presupuestoId);
}
