package com.motorlab.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motorlab.entity.Presupuesto;
import com.motorlab.entity.enums.EstadoPresupuesto;

import jakarta.persistence.LockModeType;

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Integer> {

    Optional<Presupuesto> findByOrdenId(Integer ordenId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Presupuesto p where p.id = :id")
    Optional<Presupuesto> findByIdForUpdate(@Param("id") Integer id);

    long countByEstado(EstadoPresupuesto estado);

    @Query("""
            select coalesce(sum(p.total), 0)
            from Presupuesto p
            where p.estado = com.motorlab.entity.enums.EstadoPresupuesto.autorizado
              and p.respondidoEn >= :desde
              and p.respondidoEn < :hasta
            """)
    BigDecimal sumarAutorizadosEntre(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
