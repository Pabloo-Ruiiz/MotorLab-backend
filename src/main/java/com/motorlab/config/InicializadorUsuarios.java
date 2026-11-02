package com.motorlab.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.motorlab.entity.Usuario;
import com.motorlab.repository.UsuarioRepository;

@Component
@Profile("local")
public class InicializadorUsuarios implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InicializadorUsuarios.class);
    private static final String MARCADOR_PASSWORD = "REEMPLAZAR_POR_HASH_BCRYPT";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String passwordSemilla;

    public InicializadorUsuarios(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.dev.password-semilla}") String passwordSemilla) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordSemilla = passwordSemilla;
    }

    @Override
    @Transactional
    public void run(String... argumentos) {
        List<Usuario> usuarios = usuarioRepository.findByPasswordHash(MARCADOR_PASSWORD);
        if (usuarios.isEmpty()) {
            LOG.info("No hay usuarios de ejemplo pendientes de inicializar");
            return;
        }

        String hash = passwordEncoder.encode(passwordSemilla);
        usuarios.forEach(usuario -> usuario.setPasswordHash(hash));
        usuarioRepository.saveAll(usuarios);
        usuarios.forEach(usuario -> LOG.info(
                "Contraseña semilla actualizada para el usuario de ejemplo {} ({})",
                usuario.getId(),
                usuario.getEmail()));
    }
}
