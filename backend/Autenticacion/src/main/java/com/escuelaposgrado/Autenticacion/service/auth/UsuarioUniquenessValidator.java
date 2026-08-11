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
        if (isEstudianteRole(request.getRole()) && hasText(request.getCodigoEstudiante())
                && usuarioRepository.findByCodigoEstudiante(request.getCodigoEstudiante()).isPresent()) {
            return AuthMessages.CODIGO_ESTUDIANTE_IN_USE;
        }
        if (isDocenteRole(request.getRole()) && hasText(request.getCodigoDocente())
                && usuarioRepository.findByCodigoDocente(request.getCodigoDocente()).isPresent()) {
            return AuthMessages.CODIGO_DOCENTE_IN_USE;
        }
        if (hasText(request.getDni()) && usuarioRepository.findByDni(request.getDni()).isPresent()) {
            return AuthMessages.DNI_IN_USE;
        }
        return null;
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
