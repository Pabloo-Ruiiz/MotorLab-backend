package com.motorlab.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.motorlab.entity.PresupuestoDefinitivo;

public interface PresupuestoDefinitivoRepository extends JpaRepository<PresupuestoDefinitivo, Integer> {

    @EntityGraph(attributePaths = "lineas")
    Optional<PresupuestoDefinitivo> findByOrdenId(Integer ordenId);

    boolean existsByOrdenId(Integer ordenId);
}
