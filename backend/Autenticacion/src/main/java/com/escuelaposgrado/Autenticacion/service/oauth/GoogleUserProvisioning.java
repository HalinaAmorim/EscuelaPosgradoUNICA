package com.escuelaposgrado.Autenticacion.service.oauth;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.escuelaposgrado.Autenticacion.dto.response.GoogleUserInfo;
import com.escuelaposgrado.Autenticacion.model.entity.Usuario;
import com.escuelaposgrado.Autenticacion.model.enums.Role;
import com.escuelaposgrado.Autenticacion.repository.UsuarioRepository;
import com.escuelaposgrado.Autenticacion.service.auth.AppClock;
import com.escuelaposgrado.Autenticacion.service.auth.AuthMessages;

/**
 * Localiza o cria usuários a partir do perfil Google (SRP).
 */
@Component
public class GoogleUserProvisioning {

    private static final Logger logger = LoggerFactory.getLogger(GoogleUserProvisioning.class);
    private static final String GOOGLE_PASSWORD_PREFIX = "GOOGLE_OAUTH_";
    private static final String STUDENT_CODE_PREFIX = "EST";
    private static final String EMPTY_TEXT = "";
    private static final String EMAIL_SEPARATOR = "@";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;

    public GoogleUserProvisioning(UsuarioRepository usuarioRepository, PasswordEncoder encoder) {
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
    }

    public Usuario findOrCreate(GoogleUserInfo googleUser) {
        Optional<Usuario> existingUser = usuarioRepository.findByEmail(googleUser.getEmail());
        if (existingUser.isEmpty()) {
            return createNewUser(googleUser);
        }

        Usuario usuario = existingUser.get();
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalStateException(AuthMessages.GOOGLE_ACCOUNT_DISABLED);
        }
        updateUserIfNeeded(usuario, googleUser);
        return usuario;
    }

    private void updateUserIfNeeded(Usuario usuario, GoogleUserInfo googleUser) {
        boolean needsUpdate = false;
        if (isBlank(usuario.getNombres())) {
            usuario.setNombres(nullToEmpty(googleUser.getGivenName()));
            needsUpdate = true;
        }
        if (isBlank(usuario.getApellidos())) {
            usuario.setApellidos(nullToEmpty(googleUser.getFamilyName()));
            needsUpdate = true;
        }
        if (needsUpdate) {
            usuarioRepository.save(usuario);
            logger.info("Usuario actualizado desde Google OAuth: {}", usuario.getEmail());
        }
    }

    private Usuario createNewUser(GoogleUserInfo googleUser) {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(generateUsernameFromEmail(googleUser.getEmail()));
        nuevoUsuario.setEmail(googleUser.getEmail());
        nuevoUsuario.setNombres(nullToEmpty(googleUser.getGivenName()));
        nuevoUsuario.setApellidos(nullToEmpty(googleUser.getFamilyName()));
        nuevoUsuario.setPassword(encoder.encode(GOOGLE_PASSWORD_PREFIX + System.currentTimeMillis()));

        Role userRole = determineRoleFromEmail(googleUser.getEmail());
        nuevoUsuario.setRole(userRole);
        if (userRole == Role.ALUMNO) {
            nuevoUsuario.setCodigoEstudiante(generateCodigoEstudiante());
        }
        nuevoUsuario.setActivo(true);
        nuevoUsuario.setFechaCreacion(AppClock.now());

        Usuario savedUser = usuarioRepository.save(nuevoUsuario);
        logger.info("Nuevo usuario creado desde Google OAuth: {}", savedUser.getEmail());
        return savedUser;
    }

    private String generateUsernameFromEmail(String email) {
        String baseUsername = emailLocalPart(email);
        String username = baseUsername;
        int counter = 1;
        while (usuarioRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        return username;
    }

    private Role determineRoleFromEmail(String email) {
        String localPart = emailLocalPart(email).toLowerCase();
        if (containsAny(localPart, "admin", "administrador")) {
            return Role.ADMIN;
        }
        if (containsAny(localPart, "docente", "profesor")) {
            return Role.DOCENTE;
        }
        if (localPart.contains("coordinador")) {
            return Role.COORDINADOR;
        }
        if (localPart.contains("postulante")) {
            return Role.POSTULANTE;
        }
        return Role.ALUMNO;
    }

    private String generateCodigoEstudiante() {
        int year = AppClock.now().getYear();
        String prefix = STUDENT_CODE_PREFIX + year;
        long studentCount = usuarioRepository.countByRole(Role.ALUMNO);
        String codigoEstudiante;
        int counter = 1;
        do {
            codigoEstudiante = prefix + String.format("%03d", studentCount + counter);
            counter++;
        } while (usuarioRepository.findByCodigoEstudiante(codigoEstudiante).isPresent());
        return codigoEstudiante;
    }

    private String emailLocalPart(String email) {
        return email.substring(0, email.indexOf(EMAIL_SEPARATOR));
    }

    private boolean containsAny(String value, String... fragments) {
        for (String fragment : fragments) {
            if (value.contains(fragment)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlank(String value) {
        return value == null || value.isEmpty();
    }

    private String nullToEmpty(String value) {
        return value != null ? value : EMPTY_TEXT;
    }
}
