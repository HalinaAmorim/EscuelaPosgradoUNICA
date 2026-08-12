package com.escuelaposgrado.Autenticacion.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO da resposta do Google OAuth (extraído de GoogleOAuthService).
 */
@Schema(description = "Información del usuario obtenida desde Google OAuth")
public class GoogleUserInfo {

    @Schema(description = "Identificador único del usuario en Google")
    private String sub;

    @Schema(description = "Correo electrónico verificado por Google")
    private String email;

    @JsonProperty("given_name")
    @Schema(description = "Nombres del usuario")
    private String givenName;

    @JsonProperty("family_name")
    @Schema(description = "Apellidos del usuario")
    private String familyName;

    @Schema(description = "Nombre completo reportado por Google")
    private String name;

    @Schema(description = "URL de la foto de perfil")
    private String picture;

    @JsonProperty("email_verified")
    @Schema(description = "Indica si Google verificó el correo")
    private Boolean emailVerified;

    @Schema(description = "Audiencia (aud) del id_token")
    private String aud;

    @Schema(description = "Authorized party (azp) del id_token")
    private String azp;

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getAzp() {
        return azp;
    }

    public void setAzp(String azp) {
        this.azp = azp;
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }
}
