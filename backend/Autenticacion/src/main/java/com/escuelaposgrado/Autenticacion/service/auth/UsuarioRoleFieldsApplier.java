package com.escuelaposgrado.Autenticacion.service.auth;

import org.springframework.stereotype.Component;

import com.escuelaposgrado.Autenticacion.model.entity.Usuario;
import com.escuelaposgrado.Autenticacion.model.enums.Role;

/**
 * Aplica campos específicos de rol em {@link Usuario}.
 * Centraliza o switch duplicado de AuthService (SRP / DRY).
 */
@Component
public class UsuarioRoleFieldsApplier {

    public void apply(Usuario usuario, RoleFieldsData fields) {
        clearRoleFields(usuario);

        Role role = fields.getRole();
        if (role == null) {
            return;
        }

        switch (role) {
            case ALUMNO -> applyEstudianteFields(usuario, fields, false);
            case POSTULANTE -> applyEstudianteFields(usuario, fields, true);
            case DOCENTE, COORDINADOR -> applyDocenteFields(usuario, fields);
            case ADMIN -> { /* sem campos adicionais */ }
        }
    }

    private void clearRoleFields(Usuario usuario) {
        usuario.setCodigoEstudiante(null);
        usuario.setCodigoDocente(null);
        usuario.setEspecialidad(null);
        usuario.setProgramaInteres(null);
    }

    private void applyEstudianteFields(Usuario usuario, RoleFieldsData fields, boolean includePrograma) {
        setIfPresent(fields.getCodigoEstudiante(), usuario::setCodigoEstudiante);
        if (includePrograma) {
            setIfPresent(fields.getProgramaInteres(), usuario::setProgramaInteres);
        }
    }

    private void applyDocenteFields(Usuario usuario, RoleFieldsData fields) {
        setIfPresent(fields.getCodigoDocente(), usuario::setCodigoDocente);
        setIfPresent(fields.getEspecialidad(), usuario::setEspecialidad);
    }

    private void setIfPresent(String value, java.util.function.Consumer<String> setter) {
        if (value != null && !value.trim().isEmpty()) {
            setter.accept(value.trim());
        }
    }
}
