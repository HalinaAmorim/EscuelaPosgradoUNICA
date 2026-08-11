package com.escuelaposgrado.Autenticacion.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Autenticacion.model.entity.Usuario;
import com.escuelaposgrado.Autenticacion.repository.UsuarioRepository;

/**
 * Limpieza de registros duplicados de usuarios.
 */
@Service
@Transactional
public class DataCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(DataCleanupService.class);

    private final UsuarioRepository usuarioRepository;

    public DataCleanupService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void limpiarDuplicados() {
        logger.info("Iniciando limpieza de registros duplicados...");
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            Set<Long> eliminados = new HashSet<>();

            eliminarDuplicadosPorClave(usuarios, eliminados, "username", Usuario::getUsername, u -> true);
            eliminarDuplicadosPorClave(usuarios, eliminados, "email", Usuario::getEmail, u -> true);
            eliminarDuplicadosPorClave(usuarios, eliminados, "DNI", Usuario::getDni, this::hasNonBlankDni);
            eliminarDuplicadosPorClave(
                usuarios, eliminados, "código estudiante", Usuario::getCodigoEstudiante, this::hasNonBlankCodigoEstudiante);
            eliminarDuplicadosPorClave(
                usuarios, eliminados, "código docente", Usuario::getCodigoDocente, this::hasNonBlankCodigoDocente);

            logger.info("Limpieza de duplicados completada exitosamente");
        } catch (RuntimeException e) {
            logger.error("Error durante la limpieza de duplicados: {}", e.getMessage(), e);
            throw new IllegalStateException("Error al limpiar duplicados: " + e.getMessage(), e);
        }
    }

    public boolean existenDuplicados() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        boolean hayDuplicados =
            hasDuplicateValues(usuarios, Usuario::getUsername, u -> true)
                || hasDuplicateValues(usuarios, Usuario::getEmail, u -> true)
                || hasDuplicateValues(usuarios, Usuario::getDni, this::hasNonBlankDni)
                || hasDuplicateValues(usuarios, Usuario::getCodigoEstudiante, this::hasNonBlankCodigoEstudiante)
                || hasDuplicateValues(usuarios, Usuario::getCodigoDocente, this::hasNonBlankCodigoDocente);

        if (hayDuplicados) {
            logger.warn("Se encontraron registros duplicados en la base de datos");
        }
        return hayDuplicados;
    }

    private void eliminarDuplicadosPorClave(
            List<Usuario> usuarios,
            Set<Long> eliminados,
            String campo,
            Function<Usuario, String> keyExtractor,
            Predicate<Usuario> filter) {
        Map<String, List<Usuario>> agrupados = usuarios.stream()
            .filter(u -> !eliminados.contains(u.getId()))
            .filter(filter)
            .filter(u -> keyExtractor.apply(u) != null)
            .collect(Collectors.groupingBy(keyExtractor));

        for (Map.Entry<String, List<Usuario>> entry : agrupados.entrySet()) {
            List<Usuario> duplicados = entry.getValue();
            if (duplicados.size() <= 1) {
                continue;
            }
            logger.warn("Encontrados {} duplicados para {}: {}", duplicados.size(), campo, entry.getKey());
            for (int i = 1; i < duplicados.size(); i++) {
                Usuario duplicado = duplicados.get(i);
                logger.info("Eliminando duplicado ID: {} con {}: {}", duplicado.getId(), campo, entry.getKey());
                usuarioRepository.delete(duplicado);
                eliminados.add(duplicado.getId());
            }
        }
    }

    private boolean hasDuplicateValues(
            List<Usuario> usuarios,
            Function<Usuario, String> keyExtractor,
            Predicate<Usuario> filter) {
        return usuarios.stream()
            .filter(filter)
            .filter(u -> keyExtractor.apply(u) != null)
            .collect(Collectors.groupingBy(keyExtractor, Collectors.counting()))
            .values()
            .stream()
            .anyMatch(count -> count > 1);
    }

    private boolean hasNonBlankDni(Usuario usuario) {
        return isNonBlank(usuario.getDni());
    }

    private boolean hasNonBlankCodigoEstudiante(Usuario usuario) {
        return isNonBlank(usuario.getCodigoEstudiante());
    }

    private boolean hasNonBlankCodigoDocente(Usuario usuario) {
        return isNonBlank(usuario.getCodigoDocente());
    }

    private boolean isNonBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
