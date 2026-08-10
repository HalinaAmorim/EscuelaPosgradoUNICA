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

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Postulantes", description = "Endpoints específicos para postulantes")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
             allowCredentials = "true", maxAge = 3600)
@RestController
@RequestMapping("/api/postulante")
@PreAuthorize("hasAnyRole('POSTULANTE', 'ADMIN')")
public class PostulanteController {

    private final AuthService authService;
    private final AuthenticatedUserSupport userSupport;

    public PostulanteController(AuthService authService, AuthenticatedUserSupport userSupport) {
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

    @GetMapping("/coordinadores")
    public ResponseEntity<List<UsuarioResponse>> getCoordinadores() {
        return ResponseEntity.ok(authService.getUsuariosByRole(Role.COORDINADOR));
    }

    @GetMapping("/bienvenida")
    public ResponseEntity<MessageResponse> getBienvenida(Authentication authentication) {
        return userSupport.withCurrentUser(authentication,
                usuario -> new MessageResponse(String.format(
                        "Bienvenido/a, %s. Portal de Postulantes - Escuela de Posgrado UNICA",
                        usuario.getNombreCompleto())),
                "Error al obtener información del usuario");
    }

    @GetMapping("/programa-interes")
    public ResponseEntity<MessageResponse> getProgramaInteres(Authentication authentication) {
        return userSupport.withCurrentUser(authentication, usuario -> {
            String programa = usuario.getProgramaInteres();
            return new MessageResponse("Programa de interés: " + (programa != null ? programa : "No especificado"));
        }, "Error al obtener programa de interés");
    }

    @GetMapping("/estado")
    public ResponseEntity<MessageResponse> getEstado(Authentication authentication) {
        return userSupport.withCurrentUser(authentication,
                usuario -> new MessageResponse(String.format(
                        "Estado: Postulante activo. Código: %s",
                        usuario.getCodigoEstudiante() != null
                                ? usuario.getCodigoEstudiante()
                                : "Pendiente de asignación")),
                "Error al obtener estado de postulación");
    }
}
