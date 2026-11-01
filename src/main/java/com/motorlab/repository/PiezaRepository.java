package com.motorlab.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motorlab.entity.Pieza;

import jakarta.persistence.LockModeType;

public interface PiezaRepository extends JpaRepository<Pieza, Integer> {

    Optional<Pieza> findByReferencia(String referencia);

    boolean existsByReferencia(String referencia);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pieza p where p.id = :id")
    Optional<Pieza> findByIdForUpdate(@Param("id") Integer id);

    long countByActivaTrueAndStockLessThanEqual(int umbral);

    @Query("""
            select p from Pieza p
            where (:q = ''
                   or lower(p.nombre) like lower(concat('%', :q, '%'))
                   or lower(p.referencia) like lower(concat('%', :q, '%')))
              and (:soloActivas = false or p.activa = true)
              and (:stockBajo = false or p.stock <= :umbral)
            """)
    Page<Pieza> buscar(
            @Param("q") String q,
            @Param("soloActivas") boolean soloActivas,
            @Param("stockBajo") boolean stockBajo,
            @Param("umbral") int umbral,
            Pageable pageable);
}
