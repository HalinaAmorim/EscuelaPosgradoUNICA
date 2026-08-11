package com.escuelaposgrado.Autenticacion.service.auth;

import org.springframework.stereotype.Component;

import com.escuelaposgrado.Autenticacion.dto.response.AuthResponse;
import com.escuelaposgrado.Autenticacion.dto.response.UsuarioResponse;
import com.escuelaposgrado.Autenticacion.model.entity.Usuario;

/**
 * Mapper cohesivo Usuario → DTOs de autenticação.
 * Remove Feature Envy / mapeamento duplicado em AuthService e GoogleOAuthService.
 */
@Component
public class UsuarioDtoMapper {

    public AuthResponse toAuthResponse(String jwt, Usuario usuario) {
        AuthResponse response = new AuthResponse(
            jwt,
            usuario.getId(),
            usuario.getUsername(),
            usuario.getEmail(),
            usuario.getNombres(),
            usuario.getApellidos(),
            usuario.getRole()
        );
        copyProfileFields(usuario, response);
        return response;
    }

    public UsuarioResponse toUsuarioResponse(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();
        response.setId(usuario.getId());
        response.setUsername(usuario.getUsername());
        response.setEmail(usuario.getEmail());
        response.setNombres(usuario.getNombres());
        response.setApellidos(usuario.getApellidos());
        response.setDni(usuario.getDni());
        response.setTelefono(usuario.getTelefono());
        response.setDireccion(usuario.getDireccion());
        response.setRole(usuario.getRole());
        response.setActivo(usuario.getActivo());
        response.setFechaCreacion(usuario.getFechaCreacion());
        response.setUltimoAcceso(usuario.getUltimoAcceso());
        response.setCodigoEstudiante(usuario.getCodigoEstudiante());
        response.setCodigoDocente(usuario.getCodigoDocente());
        response.setEspecialidad(usuario.getEspecialidad());
        response.setProgramaInteres(usuario.getProgramaInteres());
        return response;
    }

    private void copyProfileFields(Usuario usuario, AuthResponse response) {
        response.setDni(usuario.getDni());
        response.setTelefono(usuario.getTelefono());
        response.setDireccion(usuario.getDireccion());
        response.setUltimoAcceso(usuario.getUltimoAcceso());
        response.setCodigoEstudiante(usuario.getCodigoEstudiante());
        response.setCodigoDocente(usuario.getCodigoDocente());
        response.setEspecialidad(usuario.getEspecialidad());
        response.setProgramaInteres(usuario.getProgramaInteres());
    }
}
