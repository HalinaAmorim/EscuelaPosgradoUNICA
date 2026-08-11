package com.escuelaposgrado.Autenticacion.controller.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

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

    private static final Map<Class<? extends Throwable>, Supplier<String>> MESSAGES = new LinkedHashMap<>();

    static {
        MESSAGES.put(LockedException.class, () -> AuthMessages.ACCOUNT_LOCKED);
        MESSAGES.put(BadCredentialsException.class, () -> AuthMessages.BAD_CREDENTIALS);
        MESSAGES.put(DisabledException.class, () -> AuthMessages.ACCOUNT_DISABLED);
        MESSAGES.put(AccountExpiredException.class, () -> AuthMessages.ACCOUNT_EXPIRED);
        MESSAGES.put(CredentialsExpiredException.class, () -> AuthMessages.CREDENTIALS_EXPIRED);
    }

    private LoginExceptionTranslator() {
    }

    public static String toSpanishMessage(Throwable error) {
        Throwable current = error;
        while (current != null) {
            for (Map.Entry<Class<? extends Throwable>, Supplier<String>> entry : MESSAGES.entrySet()) {
                if (entry.getKey().isInstance(current)) {
                    return entry.getValue().get();
                }
            }
            current = current.getCause();
        }
        return error.getMessage() != null ? error.getMessage() : AuthMessages.INTERNAL_SERVER_ERROR;
    }
}
