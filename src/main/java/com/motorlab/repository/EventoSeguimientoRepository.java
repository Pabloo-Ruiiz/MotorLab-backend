package com.motorlab.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motorlab.entity.EventoSeguimiento;
import com.motorlab.entity.enums.EstadoOrden;

public interface EventoSeguimientoRepository extends JpaRepository<EventoSeguimiento, Integer> {

    List<EventoSeguimiento> findByOrdenIdOrderByFechaHoraAscIdAsc(Integer ordenId);

    boolean existsByOrdenIdAndEstado(Integer ordenId, EstadoOrden estado);
}
