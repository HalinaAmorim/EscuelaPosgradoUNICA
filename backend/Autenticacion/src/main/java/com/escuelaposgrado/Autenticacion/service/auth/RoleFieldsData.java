package com.escuelaposgrado.Autenticacion.service.auth;

import com.escuelaposgrado.Autenticacion.dto.request.ActualizarUsuarioAdminRequest;
import com.escuelaposgrado.Autenticacion.dto.request.RegistroRequest;
import com.escuelaposgrado.Autenticacion.model.enums.Role;

/**
 * Value Object com campos específicos de rol.
 * Elimina duplicação entre RegistroRequest e ActualizarUsuarioAdminRequest.
 */
public final class RoleFieldsData {

    private final Role role;
    private final String codigoEstudiante;
    private final String codigoDocente;
    private final String especialidad;
    private final String programaInteres;

    private RoleFieldsData(
            Role role,
            String codigoEstudiante,
            String codigoDocente,
            String especialidad,
            String programaInteres) {
        this.role = role;
        this.codigoEstudiante = codigoEstudiante;
        this.codigoDocente = codigoDocente;
        this.especialidad = especialidad;
        this.programaInteres = programaInteres;
    }

    public static RoleFieldsData from(RegistroRequest request) {
        return new RoleFieldsData(
            request.getRole(),
            request.getCodigoEstudiante(),
            request.getCodigoDocente(),
            request.getEspecialidad(),
            request.getProgramaInteres()
        );
    }

    public static RoleFieldsData from(ActualizarUsuarioAdminRequest request) {
        return new RoleFieldsData(
            request.getRole(),
            request.getCodigoEstudiante(),
            request.getCodigoDocente(),
            request.getEspecialidad(),
            request.getProgramaInteres()
        );
    }

    public Role getRole() {
        return role;
    }

    public String getCodigoEstudiante() {
        return codigoEstudiante;
    }

    public String getCodigoDocente() {
        return codigoDocente;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getProgramaInteres() {
        return programaInteres;
    }
}
