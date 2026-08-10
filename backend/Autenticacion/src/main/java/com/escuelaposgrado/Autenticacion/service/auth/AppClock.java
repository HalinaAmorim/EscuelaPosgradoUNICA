package com.escuelaposgrado.Autenticacion.service.auth;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Zona horaria de la aplicación (America/Lima).
 */
public final class AppClock {

    public static final ZoneId ZONE = ZoneId.of("America/Lima");

    private AppClock() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }
}
