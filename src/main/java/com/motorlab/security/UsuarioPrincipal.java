package com.motorlab.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.Rol;

public class UsuarioPrincipal implements UserDetails {

    private final Integer id;
    private final String nombre;
    private final String email;
    private final String passwordHash;
    private final Rol rol;
    private final boolean activo;

    private UsuarioPrincipal(
            Integer id,
            String nombre,
            String email,
            String passwordHash,
            Rol rol,
            boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
    }

    public static UsuarioPrincipal desde(Usuario usuario) {
        return new UsuarioPrincipal(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getRol(),
                usuario.isActivo());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name().toUpperCase()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
