package com.escuelaposgrado.Autenticacion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * DTO para actualizar información personal del usuario
 * Excluye campos críticos como: role, username, email, nombres, apellidos, dni, 
 * codigoEstudiante, codigoDocente, especialidad, programaInteres
 */
@Schema(description = "Datos que el usuario puede actualizar de su perfil")
public class ActualizarPerfilRequest {

    @Schema(description = "Número de teléfono del usuario", example = "987654321")
    @Size(max = 15, message = "El teléfono no puede tener más de 15 caracteres")
    private String telefono;

    @Schema(description = "Dirección del usuario", example = "Av. Ejemplo 123, Ica")
    @Size(max = 500, message = "La dirección no puede tener más de 500 caracteres")
    private String direccion;

    @Schema(description = "Nueva contraseña (opcional, mínimo 6 caracteres)", example = "nuevaPassword123")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Schema(description = "Confirmar nueva contraseña", example = "nuevaPassword123")
    private String confirmarPassword;

    public ActualizarPerfilRequest() {
    }

    public ActualizarPerfilRequest(String telefono, String direccion) {
        this.telefono = telefono;
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmarPassword() {
        return confirmarPassword;
    }

    public void setConfirmarPassword(String confirmarPassword) {
        this.confirmarPassword = confirmarPassword;
    }

    public boolean isPasswordValid() {
        return !isUpdatingPassword() || password.equals(confirmarPassword);
    }

    public boolean isUpdatingPassword() {
        return hasText(password);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
