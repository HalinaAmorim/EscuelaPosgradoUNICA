package com.escuelaposgrado.Autenticacion.service.auth;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.escuelaposgrado.Autenticacion.dto.request.ActualizarUsuarioAdminRequest;
import com.escuelaposgrado.Autenticacion.dto.request.RegistroRequest;
import com.escuelaposgrado.Autenticacion.model.entity.Usuario;
import com.escuelaposgrado.Autenticacion.model.enums.Role;
import com.escuelaposgrado.Autenticacion.repository.UsuarioRepository;

/**
 * Validación de unicidad de campos de usuario (SRP extraído de AuthService).
 */
@Component
public class UsuarioUniquenessValidator {

    private final UsuarioRepository usuarioRepository;

    public UsuarioUniquenessValidator(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String validateForRegistro(RegistroRequest request) {
        String estudianteError = validateCodigoEstudiante(request);
        if (estudianteError != null) {
            return estudianteError;
        }
        String docenteError = validateCodigoDocente(request);
        if (docenteError != null) {
            return docenteError;
        }
        return validateDni(request);
    }

    public String validateAdminUsernameEmail(Long id, Usuario current, ActualizarUsuarioAdminRequest request) {
        String usernameError = conflictIfChanged(
                current.getUsername(), request.getUsername(), id, usuarioRepository::findByUsername,
                AuthMessages.USERNAME_IN_USE_SHORT);
        if (usernameError != null) {
            return usernameError;
        }
        return conflictIfChanged(
                current.getEmail(), request.getEmail(), id, usuarioRepository::findByEmail,
                AuthMessages.EMAIL_IN_USE_SHORT);
    }

    private String validateCodigoEstudiante(RegistroRequest request) {
        if (!isEstudianteRole(request.getRole()) || !hasText(request.getCodigoEstudiante())) {
            return null;
        }
        return presentOrNull(
                usuarioRepository.findByCodigoEstudiante(request.getCodigoEstudiante()),
                AuthMessages.CODIGO_ESTUDIANTE_IN_USE);
    }

    private String validateCodigoDocente(RegistroRequest request) {
        if (!isDocenteRole(request.getRole()) || !hasText(request.getCodigoDocente())) {
            return null;
        }
        return presentOrNull(
                usuarioRepository.findByCodigoDocente(request.getCodigoDocente()),
                AuthMessages.CODIGO_DOCENTE_IN_USE);
    }

    private String validateDni(RegistroRequest request) {
        if (!hasText(request.getDni())) {
            return null;
        }
        return presentOrNull(usuarioRepository.findByDni(request.getDni()), AuthMessages.DNI_IN_USE);
    }

    private String conflictIfChanged(
            String currentValue,
            String newValue,
            Long id,
            Function<String, Optional<Usuario>> finder,
            String errorMessage) {
        if (currentValue.equals(newValue)) {
            return null;
        }
        Optional<Usuario> existing = finder.apply(newValue);
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            return errorMessage;
        }
        return null;
    }

    private String presentOrNull(Optional<Usuario> existing, String errorMessage) {
        return existing.isPresent() ? errorMessage : null;
    }

    private boolean isEstudianteRole(Role role) {
        return role == Role.ALUMNO || role == Role.POSTULANTE;
    }

    private boolean isDocenteRole(Role role) {
        return role == Role.DOCENTE || role == Role.COORDINADOR;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
