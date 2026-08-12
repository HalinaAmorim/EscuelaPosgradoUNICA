package com.escuelaposgrado.Autenticacion.model.enums;

/**
 * Enumeración de roles disponibles en el sistema académico
 * de la Escuela de Posgrado UNICA
 */
public enum Role {
    ADMIN,
    DOCENTE,
    ALUMNO,
    COORDINADOR,
    POSTULANTE;

    private static final String AUTHORITY_PREFIX = "ROLE_";

    public String asAuthority() {
        return AUTHORITY_PREFIX + name();
    }
}
