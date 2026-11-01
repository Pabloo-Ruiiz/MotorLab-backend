package com.motorlab.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.motorlab.entity.Notificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    Page<Notificacion> findByUsuarioIdOrderByIdDesc(Integer usuarioId, Pageable pageable);

    Page<Notificacion> findByUsuarioIdAndLeidaOrderByIdDesc(
            Integer usuarioId, boolean leida, Pageable pageable);

    Optional<Notificacion> findByIdAndUsuarioId(Integer id, Integer usuarioId);

    long countByUsuarioIdAndLeidaFalse(Integer usuarioId);

    @Modifying(clearAutomatically = true)
    @Query("update Notificacion n set n.leida = true where n.usuario.id = :usuarioId and n.leida = false")
    int marcarTodasLeidas(@Param("usuarioId") Integer usuarioId);
}
