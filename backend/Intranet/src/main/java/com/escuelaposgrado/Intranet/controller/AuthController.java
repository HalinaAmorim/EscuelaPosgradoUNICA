package com.escuelaposgrado.Intranet.controller;

import com.escuelaposgrado.Intranet.dto.MensajeResponse;
import com.escuelaposgrado.Intranet.dto.UsuarioDTO;
import com.escuelaposgrado.Intranet.security.jwt.JwtUtils;
import com.escuelaposgrado.Intranet.service.UsuarioService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para autenticación y autorización
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final String INVALID_CREDENTIALS = "Error: Credenciales inválidas";
    private static final String REGISTER_OK = "Usuario registrado exitosamente";
    private static final String LOGOUT_OK = "Sesión cerrada exitosamente";
    private static final String INVALID_TOKEN = "Token inválido";
    private static final String VERIFY_ERROR = "Error al verificar token";

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UsuarioService usuarioService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwtToken(authentication);
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(userDetails.getUsername());

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    userDetails.getUsername(),
                    usuario.getRol(),
                    usuario.getNombreCompleto()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeResponse(INVALID_CREDENTIALS));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        try {
            usuarioService.crearUsuario(usuarioDTO);
            return ResponseEntity.ok(new MensajeResponse(REGISTER_OK));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MensajeResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MensajeResponse(LOGOUT_OK));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            if (!jwtUtils.validateJwtToken(jwt)) {
                return ResponseEntity.badRequest().body(new MensajeResponse(INVALID_TOKEN));
            }
            String username = jwtUtils.getUserNameFromJwtToken(jwt);
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(username);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MensajeResponse(VERIFY_ERROR));
        }
    }
}

/**
 * Request para login
 */
class LoginRequest {
    private String username;
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

/**
 * Response con JWT
 */
class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String role;
    private String nombreCompleto;

    public JwtResponse(String accessToken, String username, String role, String nombreCompleto) {
        this.token = accessToken;
        this.username = username;
        this.role = role;
        this.nombreCompleto = nombreCompleto;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
}
