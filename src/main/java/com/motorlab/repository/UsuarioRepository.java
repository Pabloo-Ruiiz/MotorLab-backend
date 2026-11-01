package com.motorlab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.Rol;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Usuario> findByRol(Rol rol, Pageable pageable);

    List<Usuario> findByRolAndActivoTrue(Rol rol);
}
