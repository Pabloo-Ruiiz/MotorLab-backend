package com.motorlab.service;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.dto.common.PageResponse;
import com.motorlab.dto.usuario.UsuarioCreateRequest;
import com.motorlab.dto.usuario.UsuarioResponse;
import com.motorlab.dto.usuario.UsuarioUpdateRequest;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.Rol;
import com.motorlab.exception.EmailDuplicadoException;
import com.motorlab.exception.RecursoNoEncontradoException;
import com.motorlab.exception.SolicitudInvalidaException;
import com.motorlab.mapper.UsuarioMapper;
import com.motorlab.repository.UsuarioRepository;
import com.motorlab.security.SecurityUtils;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listar(Rol rol, Integer pagina, Integer limite) {
        Pageable pageable = PageResponse.pageable(pagina, limite);
        Page<Usuario> usuarios = rol == null
                ? usuarioRepository.findAll(pageable)
                : usuarioRepository.findByRol(rol, pageable);
        return PageResponse.de(usuarios.map(usuarioMapper::aResponse));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest solicitud) {
        String email = normalizarEmail(solicitud.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailDuplicadoException();
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setEmail(email);
        usuario.setTelefono(solicitud.telefono().trim());
        usuario.setPasswordHash(passwordEncoder.encode(solicitud.password()));
        usuario.setRol(solicitud.rol());
        usuario.setActivo(true);

        return usuarioMapper.aResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioUpdateRequest solicitud) {
        if (!solicitud.tieneCambios()) {
            throw new SolicitudInvalidaException("Debe indicar al menos el rol o el estado activo");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        boolean esElPropioUsuario = id.equals(SecurityUtils.idActual());
        if (esElPropioUsuario
                && ((solicitud.activo() != null && !solicitud.activo())
                        || (solicitud.rol() != null && solicitud.rol() != Rol.admin))) {
            throw new SolicitudInvalidaException(
                    "Un administrador no puede desactivarse ni cambiarse el rol a sí mismo");
        }

        if (solicitud.rol() != null) {
            usuario.setRol(solicitud.rol());
        }
        if (solicitud.activo() != null) {
            usuario.setActivo(solicitud.activo());
        }
        return usuarioMapper.aResponse(usuario);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
