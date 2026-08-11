package com.escuelaposgrado.Autenticacion.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.escuelaposgrado.Autenticacion.config.CorsOrigins;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.escuelaposgrado.Autenticacion.controller.support.AuthenticatedUserSupport;
import com.escuelaposgrado.Autenticacion.dto.response.MessageResponse;
import com.escuelaposgrado.Autenticacion.dto.response.UsuarioResponse;
import com.escuelaposgrado.Autenticacion.model.enums.Role;
import com.escuelaposgrado.Autenticacion.service.AuthService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Coordinadores", description = "Endpoints específicos para coordinadores académicos")
@CrossOrigin(origins = {CorsOrigins.LOCALHOST, CorsOrigins.LOCALHOST_IP},
             allowCredentials = "true", maxAge = 3600)
@RestController
@RequestMapping("/api/coordinador")
@PreAuthorize("hasAnyRole('COORDINADOR', 'ADMIN')")
public class CoordinadorController {

    private final AuthService authService;
    private final AuthenticatedUserSupport userSupport;

    public CoordinadorController(AuthService authService, AuthenticatedUserSupport userSupport) {
        this.authService = authService;
        this.userSupport = userSupport;
    }

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> getPerfil(Authentication authentication) {
        return userSupport.getCurrentUserOrBadRequest(authentication);
    }

    @GetMapping("/docentes")
    public ResponseEntity<List<UsuarioResponse>> getDocentes() {
        return ResponseEntity.ok(authService.getUsuariosByRole(Role.DOCENTE));
    }

    @GetMapping("/alumnos")
    public ResponseEntity<List<UsuarioResponse>> getAlumnos() {
        return ResponseEntity.ok(authService.getUsuariosByRole(Role.ALUMNO));
    }

    @GetMapping("/postulantes")
    public ResponseEntity<List<UsuarioResponse>> getPostulantes() {
        return ResponseEntity.ok(authService.getUsuariosByRole(Role.POSTULANTE));
    }

    @GetMapping("/coordinadores")
    public ResponseEntity<List<UsuarioResponse>> getCoordinadores() {
        return ResponseEntity.ok(authService.getUsuariosByRole(Role.COORDINADOR));
    }

    @GetMapping("/bienvenida")
    public ResponseEntity<MessageResponse> getBienvenida(Authentication authentication) {
        return userSupport.withCurrentUser(authentication,
                usuario -> new MessageResponse(String.format(
                        "Bienvenido/a, %s. Panel de Coordinación Académica - Escuela de Posgrado UNICA",
                        usuario.getNombreCompleto())),
                "Error al obtener información del usuario");
    }

    @GetMapping("/resumen")
    public ResponseEntity<MessageResponse> getResumen() {
        MessageResponse estadisticas = authService.getEstadisticas();
        return ResponseEntity.ok(new MessageResponse("Resumen académico: " + estadisticas.getMessage()));
    }
}
