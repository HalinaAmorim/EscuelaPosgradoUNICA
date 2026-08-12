package com.escuelaposgrado.Intranet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.escuelaposgrado.Intranet.dto.UsuarioDTO;
import com.escuelaposgrado.Intranet.security.jwt.JwtUtils;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UsuarioService usuarioService;

    public Authentication authenticate(String username, String password) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password));

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        return authentication;
    }

    public String generateToken(Authentication authentication) {
        return jwtUtils.generateJwtToken(authentication);
    }

    public UsuarioDTO getUser(String username) {
        return usuarioService.obtenerUsuarioPorEmail(username);
    }

    public void register(UsuarioDTO usuarioDTO) {
        usuarioService.crearUsuario(usuarioDTO);
    }

    public boolean validateToken(String token) {
        return jwtUtils.validateJwtToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtUtils.getUserNameFromJwtToken(token);
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }
}