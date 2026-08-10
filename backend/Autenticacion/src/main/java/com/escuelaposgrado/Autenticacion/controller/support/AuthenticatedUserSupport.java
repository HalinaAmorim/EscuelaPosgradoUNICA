package com.escuelaposgrado.Autenticacion.controller.support;

import java.util.function.Function;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.escuelaposgrado.Autenticacion.dto.response.MessageResponse;
import com.escuelaposgrado.Autenticacion.dto.response.UsuarioResponse;
import com.escuelaposgrado.Autenticacion.service.AuthService;

/**
 * Helpers compartidos por controladores de perfil por rol (DRY).
 */
@Component
public class AuthenticatedUserSupport {

    private final AuthService authService;

    public AuthenticatedUserSupport(AuthService authService) {
        this.authService = authService;
    }

    public ResponseEntity<UsuarioResponse> getCurrentUserOrBadRequest(Authentication authentication) {
        try {
            return ResponseEntity.ok(authService.getCurrentUser(authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<MessageResponse> withCurrentUser(
            Authentication authentication,
            Function<UsuarioResponse, MessageResponse> mapper,
            String errorMessage) {
        try {
            UsuarioResponse usuario = authService.getCurrentUser(authentication.getName());
            return ResponseEntity.ok(mapper.apply(usuario));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(errorMessage, false));
        }
    }
}
