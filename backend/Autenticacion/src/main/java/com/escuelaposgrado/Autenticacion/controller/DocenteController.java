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

@Tag(name = "Docentes", description = "Endpoints específicos para docentes")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
             allowCredentials = "true", maxAge = 3600)
@RestController
@RequestMapping("/api/docente")
@PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'ADMIN')")
public class DocenteController {

    private final AuthService authService;
    private final AuthenticatedUserSupport userSupport;

    public DocenteController(AuthService authService, AuthenticatedUserSupport userSupport) {
        this.authService = authService;
        this.userSupport = userSupport;
    }

    @Operation(
            summary = "Obtener perfil del docente",
            description = "Información del perfil del docente autenticado",
            security = @SecurityRequirement(name = "bearerAuth"),
            tags = {"Docentes"}
    )
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> getPerfil(Authentication authentication) {
        return userSupport.getCurrentUserOrBadRequest(authentication);
    }

    @GetMapping("/alumnos")
    public ResponseEntity<List<UsuarioResponse>> getAlumnos() {
        return ResponseEntity.ok(authService.getUsuariosByRole(Role.ALUMNO));
    }

    @GetMapping("/colegas")
    public ResponseEntity<List<UsuarioResponse>> getColegas() {
        return ResponseEntity.ok(authService.getUsuariosByRole(Role.DOCENTE));
    }

    @GetMapping("/bienvenida")
    public ResponseEntity<MessageResponse> getBienvenida(Authentication authentication) {
        return userSupport.withCurrentUser(authentication,
                usuario -> new MessageResponse(String.format(
                        "Bienvenido/a, %s. Panel de Docente - Escuela de Posgrado UNICA",
                        usuario.getNombreCompleto())),
                "Error al obtener información del usuario");
    }
}
