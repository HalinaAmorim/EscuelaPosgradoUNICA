package com.escuelaposgrado.Autenticacion.service.auth;

/**
 * Constantes de mensagens de autenticação (elimina magic strings / S1192).
 */
public final class AuthMessages {

    public static final String USERNAME_IN_USE = "Error: El nombre de usuario ya está en uso!";
    public static final String USERNAME_IN_USE_SHORT = "Error: El nombre de usuario ya está en uso";
    public static final String EMAIL_IN_USE = "Error: El email ya está en uso!";
    public static final String EMAIL_IN_USE_SHORT = "Error: El email ya está en uso";
    public static final String USER_NOT_FOUND = "Error: Usuario no encontrado";
    public static final String USER_NOT_FOUND_PLAIN = "Usuario no encontrado";
    public static final String PASSWORDS_MISMATCH = "Error: Las contraseñas no coinciden";
    public static final String NEW_PASSWORDS_MISMATCH = "Error: Las nuevas contraseñas no coinciden";
    public static final String CURRENT_PASSWORD_INVALID = "Error: La contraseña actual es incorrecta";
    public static final String NEW_PASSWORD_SAME = "Error: La nueva contraseña debe ser diferente a la actual";
    public static final String PROFILE_UPDATED = "Perfil actualizado exitosamente";
    public static final String PASSWORD_CHANGED = "Contraseña cambiada exitosamente";
    public static final String USER_REGISTERED = "Usuario registrado exitosamente";
    public static final String USER_DEACTIVATED = "Usuario desactivado exitosamente";
    public static final String USER_ACTIVATED = "Usuario activado exitosamente";
    public static final String USER_UPDATED = "Usuario actualizado exitosamente";
    public static final String INTERNAL_ERROR_PREFIX = "Error interno del servidor: ";
    public static final String CODIGO_ESTUDIANTE_IN_USE = "El código de estudiante ya está en uso!";
    public static final String CODIGO_DOCENTE_IN_USE = "El código de docente ya está en uso!";
    public static final String DNI_IN_USE = "El DNI ya está registrado!";
    public static final String USER_NOT_FOUND_WITH_CREDENTIAL = "Usuario no encontrado con username o email: ";
    public static final String ACCOUNT_LOCKED = "La cuenta de usuario está desactivada";
    public static final String BAD_CREDENTIALS = "Credenciales incorrectas";
    public static final String ACCOUNT_DISABLED = "La cuenta está deshabilitada";
    public static final String ACCOUNT_EXPIRED = "La cuenta ha expirado";
    public static final String CREDENTIALS_EXPIRED = "Las credenciales han expirado";
    public static final String GOOGLE_TOKEN_INVALID = "Token de Google inválido";
    public static final String GOOGLE_EMAIL_NOT_INSTITUTIONAL =
            "Solo se permiten correos institucionales (@unica.edu.pe)";
    public static final String GOOGLE_ACCOUNT_DISABLED =
            "La cuenta está desactivada. Contacte al administrador.";
    public static final String GOOGLE_INTERNAL_ERROR = "Error interno en autenticación con Google";
    public static final String INTERNAL_SERVER_ERROR = "Error interno del servidor";

    private AuthMessages() {
    }
}
