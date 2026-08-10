package com.escuelaposgrado.Autenticacion.controller.support;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import com.escuelaposgrado.Autenticacion.service.auth.AuthMessages;

/**
 * Traduce excepciones de Spring Security a mensajes en español (sin sniffing de strings).
 */
public final class LoginExceptionTranslator {

    private LoginExceptionTranslator() {
    }

    public static String toSpanishMessage(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof LockedException) {
                return AuthMessages.ACCOUNT_LOCKED;
            }
            if (current instanceof BadCredentialsException) {
                return AuthMessages.BAD_CREDENTIALS;
            }
            if (current instanceof DisabledException) {
                return AuthMessages.ACCOUNT_DISABLED;
            }
            if (current instanceof AccountExpiredException) {
                return AuthMessages.ACCOUNT_EXPIRED;
            }
            if (current instanceof CredentialsExpiredException) {
                return AuthMessages.CREDENTIALS_EXPIRED;
            }
            current = current.getCause();
        }
        return error.getMessage() != null ? error.getMessage() : AuthMessages.INTERNAL_SERVER_ERROR;
    }
}
