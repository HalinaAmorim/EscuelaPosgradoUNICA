package com.escuelaposgrado.Autenticacion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.escuelaposgrado.Autenticacion.model.entity.Usuario;
import com.escuelaposgrado.Autenticacion.repository.UsuarioRepository;
import com.escuelaposgrado.Autenticacion.service.DataCleanupService;

/**
 * Inicialización de datos por defecto del sistema.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataCleanupService dataCleanupService;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            DataCleanupService dataCleanupService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.dataCleanupService = dataCleanupService;
    }

    @Override
    public void run(String... args) {
        logger.info("Verificando y limpiando registros duplicados...");
        if (dataCleanupService.existenDuplicados()) {
            dataCleanupService.limpiarDuplicados();
            logger.info("Duplicados eliminados exitosamente");
        } else {
            logger.info("No se encontraron duplicados");
        }
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        try {
            createIfAbsent("admin", DemoUserFactory.admin(passwordEncoder));
            createSampleUsers();
        } catch (RuntimeException e) {
            logger.error("Error inicializando datos: {}", e.getMessage());
        }
    }

    private void createSampleUsers() {
        createIfAbsent("docente.demo", DemoUserFactory.docente(passwordEncoder));
        createIfAbsent("coordinador.demo", DemoUserFactory.coordinador(passwordEncoder));
        createIfAbsent("alumno.demo", DemoUserFactory.alumno(passwordEncoder));
        createIfAbsent("postulante.demo", DemoUserFactory.postulante(passwordEncoder));
        logger.info("Inicialización de datos completada");
    }

    private void createIfAbsent(String username, Usuario usuario) {
        if (usuarioRepository.existsByUsername(username)) {
            return;
        }
        usuarioRepository.save(usuario);
        logger.info("Usuario creado: {}", username);
    }
}
