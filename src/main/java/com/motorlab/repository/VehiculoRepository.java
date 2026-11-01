package com.motorlab.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.motorlab.entity.Vehiculo;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    Page<Vehiculo> findByUsuarioIdAndActivoTrue(Integer usuarioId, Pageable pageable);

    Page<Vehiculo> findByActivoTrue(Pageable pageable);

    boolean existsByMatricula(String matricula);
}
