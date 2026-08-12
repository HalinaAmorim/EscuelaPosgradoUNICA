package com.escuelaposgrado.Intranet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.escuelaposgrado.Intranet.dto.MensajeResponse;
import com.escuelaposgrado.Intranet.dto.UsuarioDTO;
import com.escuelaposgrado.Intranet.service.AuthService;

import jakarta.validation.Valid;

/**
 * Controlador para autenticación y autorización.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Iniciar sesión.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest) {

        try {
            Authentication authentication = authService.authenticate(
                    loginRequest.getUsername(),
                    loginRequest.getPassword());

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String jwt = authService.generateToken(authentication);

            UsuarioDTO usuario = authService.getUser(userDetails.getUsername());

            return ResponseEntity.ok(
                    new JwtResponse(
                            jwt,
                            userDetails.getUsername(),
                            usuario.getRol(),
                            usuario.getNombreCompleto()));

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MensajeResponse(
                            "Error: Credenciales inválidas"));
        }
    }

    /**
     * Registrar nuevo usuario.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody UsuarioDTO usuarioDTO) {

        try {
            authService.register(usuarioDTO);

            return ResponseEntity.ok(
                    new MensajeResponse(
                            "Usuario registrado exitosamente"));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MensajeResponse(
                            "Error: " + e.getMessage()));
        }
    }

    /**
     * Cerrar sesión.
     */
    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {

        authService.logout();

        return ResponseEntity.ok(
                new MensajeResponse(
                        "Sesión cerrada exitosamente"));
    }

    /**
     * Verificar token.
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(
            @RequestHeader("Authorization") String token) {

        try {
            String jwt = token.substring(7);

            if (!authService.validateToken(jwt)) {
                return ResponseEntity
                        .badRequest()
                        .body(new MensajeResponse(
                                "Token inválido"));
            }

            String username = authService.getUsernameFromToken(jwt);

            UsuarioDTO usuario = authService.getUser(username);

            return ResponseEntity.ok(usuario);

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MensajeResponse(
                            "Error al verificar token"));
        }
    }
}

/**
 * Request para login.
 */
class LoginRequest {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

/**
 * Response con JWT.
 */
class JwtResponse {

    private String token;
    private String type = "Bearer";
    private String username;
    private String role;
    private String nombreCompleto;

    public JwtResponse(
            String accessToken,
            String username,
            String role,
            String nombreCompleto) {

        this.token = accessToken;
        this.username = username;
        this.role = role;
        this.nombreCompleto = nombreCompleto;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
}