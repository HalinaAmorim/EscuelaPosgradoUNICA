package com.escuelaposgrado.Autenticacion.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.escuelaposgrado.Autenticacion.controller.support.AuthenticatedUserSupport;
import com.escuelaposgrado.Autenticacion.dto.response.MessageResponse;
import com.escuelaposgrado.Autenticacion.dto.response.UsuarioResponse;
import com.escuelaposgrado.Autenticacion.model.enums.Role;
import com.escuelaposgrado.Autenticacion.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Alumnos", description = "Endpoints específicos para estudiantes")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
             allowCredentials = "true", maxAge = 3600)
@RestController
@RequestMapping("/api/alumno")
@PreAuthorize("hasAnyRole('ALUMNO', 'ADMIN')")
public class AlumnoController {

    private final AuthService authService;
    private final AuthenticatedUserSupport userSupport;

    public AlumnoController(AuthService authService, AuthenticatedUserSupport userSupport) {
        this.authService = authService;
        this.userSupport = userSupport;
    }

    @Operation(
            summary = "Obtener perfil del alumno",
            description = "Information del perfil del alumno autenticado",
            security = @SecurityRequirement(name = "bearerAuth"),
            tags = {"Alumnos"}
    )
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> getPerfil(Authentication authentication) {
        return userSupport.getCurrentUserOrBadRequest(authentication);
    }

    @GetMapping("/docentes")
    public ResponseEntity<List<UsuarioResponse>> getDocentes() {
        return ResponseEntity.ok(authService.getUsuariosByRole(Role.DOCENTE));
    }

    @GetMapping("/companeros")
    public ResponseEntity<List<UsuarioResponse>> getCompaneros() {
        return ResponseEntity.ok(authService.getUsuariosByRole(Role.ALUMNO));
    }

    @GetMapping("/bienvenida")
    public ResponseEntity<MessageResponse> getBienvenida(Authentication authentication) {
        return userSupport.withCurrentUser(authentication,
                usuario -> new MessageResponse(String.format(
                        "Bienvenido/a, %s. Portal del Estudiante - Escuela de Posgrado UNICA",
                        usuario.getNombreCompleto())),
                "Error al obtener información del usuario");
    }

    @GetMapping("/codigo")
    public ResponseEntity<MessageResponse> getCodigoEstudiante(Authentication authentication) {
        return userSupport.withCurrentUser(authentication,
                usuario -> new MessageResponse("Código de estudiante: " +
                        (usuario.getCodigoEstudiante() != null ? usuario.getCodigoEstudiante() : "No asignado")),
                "Error al obtener código de estudiante");
    }
}
