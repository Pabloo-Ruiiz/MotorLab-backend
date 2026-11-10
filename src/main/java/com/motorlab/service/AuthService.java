package com.motorlab.service;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.dto.auth.LoginRequest;
import com.motorlab.dto.auth.LoginResponse;
import com.motorlab.dto.auth.RegistroRequest;
import com.motorlab.dto.usuario.UsuarioResponse;
import com.motorlab.entity.Usuario;
import com.motorlab.entity.enums.Rol;
import com.motorlab.exception.EmailDuplicadoException;
import com.motorlab.exception.EmailNoVerificadoException;
import com.motorlab.exception.RecursoNoEncontradoException;
import com.motorlab.mapper.UsuarioMapper;
import com.motorlab.repository.UsuarioRepository;
import com.motorlab.security.JwtService;
import com.motorlab.security.SecurityUtils;
import com.motorlab.security.UsuarioPrincipal;

@Service
public class AuthService {

    private static final String MENSAJE_CREDENCIALES = "Credenciales incorrectas";

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final VerificacionEmailService verificacionEmailService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            VerificacionEmailService verificacionEmailService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.verificacionEmailService = verificacionEmailService;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest solicitud) {
        String email = normalizarEmail(solicitud.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailDuplicadoException();
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setEmail(email);
        usuario.setTelefono(solicitud.telefono().trim());
        usuario.setPasswordHash(passwordEncoder.encode(solicitud.password()));
        usuario.setRol(Rol.cliente);
        usuario.setActivo(true);
        usuario.setEmailVerificado(false);
        verificacionEmailService.emitirToken(usuario);

        return usuarioMapper.aResponse(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest solicitud) {
        String email = normalizarEmail(solicitud.email());
        Authentication autenticacion;
        try {
            autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, solicitud.password()));
        } catch (AuthenticationException excepcion) {
            throw new BadCredentialsException(MENSAJE_CREDENCIALES);
        }

        if (!(autenticacion.getPrincipal() instanceof UsuarioPrincipal principal)
                || !principal.isEnabled()) {
            throw new BadCredentialsException(MENSAJE_CREDENCIALES);
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new BadCredentialsException(MENSAJE_CREDENCIALES));

        // Después de comprobar la contraseña, para no revelar el estado de cuentas ajenas
        if (!usuario.isEmailVerificado()) {
            throw new EmailNoVerificadoException();
        }

        return new LoginResponse(jwtService.generarToken(principal), usuarioMapper.aResponse(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse yo() {
        UsuarioPrincipal principal = SecurityUtils.usuarioActual();
        return usuarioRepository.findById(principal.getId())
                .map(usuarioMapper::aResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
