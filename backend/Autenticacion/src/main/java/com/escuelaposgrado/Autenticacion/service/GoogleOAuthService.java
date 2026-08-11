package com.escuelaposgrado.Autenticacion.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Autenticacion.dto.request.GoogleLoginRequest;
import com.escuelaposgrado.Autenticacion.dto.response.AuthResponse;
import com.escuelaposgrado.Autenticacion.dto.response.GoogleUserInfo;
import com.escuelaposgrado.Autenticacion.model.entity.Usuario;
import com.escuelaposgrado.Autenticacion.repository.UsuarioRepository;
import com.escuelaposgrado.Autenticacion.security.jwt.JwtUtils;
import com.escuelaposgrado.Autenticacion.service.auth.AppClock;
import com.escuelaposgrado.Autenticacion.service.auth.AuthMessages;
import com.escuelaposgrado.Autenticacion.service.auth.UsuarioDtoMapper;
import com.escuelaposgrado.Autenticacion.service.oauth.GoogleTokenVerifier;
import com.escuelaposgrado.Autenticacion.service.oauth.GoogleUserProvisioning;

/**
 * Orquesta autenticación Google OAuth (verificación + provisioning + JWT).
 */
@Service
@Transactional
public class GoogleOAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuthService.class);
    private static final String UNIVERSITY_EMAIL_SUFFIX = "@unica.edu.pe";

    private final UsuarioRepository usuarioRepository;
    private final JwtUtils jwtUtils;
    private final UsuarioDtoMapper usuarioDtoMapper;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final GoogleUserProvisioning googleUserProvisioning;

    public GoogleOAuthService(
            UsuarioRepository usuarioRepository,
            JwtUtils jwtUtils,
            UsuarioDtoMapper usuarioDtoMapper,
            GoogleTokenVerifier googleTokenVerifier,
            GoogleUserProvisioning googleUserProvisioning) {
        this.usuarioRepository = usuarioRepository;
        this.jwtUtils = jwtUtils;
        this.usuarioDtoMapper = usuarioDtoMapper;
        this.googleTokenVerifier = googleTokenVerifier;
        this.googleUserProvisioning = googleUserProvisioning;
    }

    public AuthResponse authenticateWithGoogle(GoogleLoginRequest request) {
        try {
            GoogleUserInfo googleUser = googleTokenVerifier.verify(request.getGoogleToken());
            if (googleUser == null) {
                throw new IllegalArgumentException(AuthMessages.GOOGLE_TOKEN_INVALID);
            }
            if (!isUniversityEmail(googleUser.getEmail())) {
                throw new IllegalArgumentException(AuthMessages.GOOGLE_EMAIL_NOT_INSTITUTIONAL);
            }

            Usuario usuario = googleUserProvisioning.findOrCreate(googleUser);
            usuarioRepository.actualizarUltimoAcceso(usuario.getId(), AppClock.now());
            String jwt = jwtUtils.generateJwtTokenForUser(usuario.getUsername(), usuario.getAuthorities());
            return usuarioDtoMapper.toAuthResponse(jwt, usuario);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error en autenticación con Google: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("Error en autenticación con Google: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado en autenticación con Google: {}", e.getMessage(), e);
            throw new IllegalStateException(AuthMessages.GOOGLE_INTERNAL_ERROR, e);
        }
    }

    private boolean isUniversityEmail(String email) {
        return email != null && email.toLowerCase().endsWith(UNIVERSITY_EMAIL_SUFFIX);
    }
}
