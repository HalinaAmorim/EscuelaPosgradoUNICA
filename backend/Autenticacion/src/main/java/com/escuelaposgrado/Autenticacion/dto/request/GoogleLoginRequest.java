package com.escuelaposgrado.Autenticacion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para las peticiones de login con Google OAuth
 */
@Schema(description = "Datos requeridos para el login con Google OAuth")
public class GoogleLoginRequest {

    @Schema(
            description = "Token de Google OAuth obtenido del frontend",
            example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El token de Google es obligatorio")
    private String googleToken;

    public GoogleLoginRequest() {
    }

    public GoogleLoginRequest(String googleToken) {
        this.googleToken = googleToken;
    }

    public String getGoogleToken() {
        return googleToken;
    }

    public void setGoogleToken(String googleToken) {
        this.googleToken = googleToken;
    }
}
