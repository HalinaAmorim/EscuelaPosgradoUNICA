package com.escuelaposgrado.Autenticacion.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Autenticacion.dto.request.ActualizarPerfilRequest;
import com.escuelaposgrado.Autenticacion.dto.request.ActualizarUsuarioAdminRequest;
import com.escuelaposgrado.Autenticacion.dto.request.CambiarPasswordRequest;
import com.escuelaposgrado.Autenticacion.dto.request.LoginRequest;
import com.escuelaposgrado.Autenticacion.dto.request.RegistroRequest;
import com.escuelaposgrado.Autenticacion.dto.response.AuthResponse;
import com.escuelaposgrado.Autenticacion.dto.response.MessageResponse;
import com.escuelaposgrado.Autenticacion.dto.response.UsuarioResponse;
import com.escuelaposgrado.Autenticacion.model.entity.Usuario;
import com.escuelaposgrado.Autenticacion.model.enums.Role;
import com.escuelaposgrado.Autenticacion.repository.UsuarioRepository;
import com.escuelaposgrado.Autenticacion.security.jwt.JwtUtils;
import com.escuelaposgrado.Autenticacion.service.auth.AppClock;
import com.escuelaposgrado.Autenticacion.service.auth.AuthMessages;
import com.escuelaposgrado.Autenticacion.service.auth.RoleFieldsData;
import com.escuelaposgrado.Autenticacion.service.auth.UsuarioDtoMapper;
import com.escuelaposgrado.Autenticacion.service.auth.UsuarioRoleFieldsApplier;
import com.escuelaposgrado.Autenticacion.service.auth.UsuarioUniquenessValidator;

/**
 * Autenticación, perfil y administración de usuarios.
 * Validación de unicidad y mapeo delegados a componentes cohesivos.
 */
@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final UsuarioDtoMapper usuarioDtoMapper;
    private final UsuarioRoleFieldsApplier roleFieldsApplier;
    private final UsuarioUniquenessValidator uniquenessValidator;

    public AuthService(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            PasswordEncoder encoder,
            JwtUtils jwtUtils,
            UsuarioDtoMapper usuarioDtoMapper,
            UsuarioRoleFieldsApplier roleFieldsApplier,
            UsuarioUniquenessValidator uniquenessValidator) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.usuarioDtoMapper = usuarioDtoMapper;
        this.roleFieldsApplier = roleFieldsApplier;
        this.uniquenessValidator = uniquenessValidator;
    }

    public AuthResponse login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        Usuario usuario = (Usuario) authentication.getPrincipal();
        usuarioRepository.actualizarUltimoAcceso(usuario.getId(), AppClock.now());
        return usuarioDtoMapper.toAuthResponse(jwt, usuario);
    }

    public MessageResponse registro(RegistroRequest registroRequest) {
        if (usuarioRepository.existsByUsername(registroRequest.getUsername())) {
            return failure(AuthMessages.USERNAME_IN_USE);
        }
        if (usuarioRepository.existsByEmail(registroRequest.getEmail())) {
            return failure(AuthMessages.EMAIL_IN_USE);
        }

        String validationError = uniquenessValidator.validateForRegistro(registroRequest);
        if (validationError != null) {
            return failure("Error: " + validationError);
        }

        Usuario usuario = new Usuario(
                registroRequest.getUsername(),
                registroRequest.getEmail(),
                encoder.encode(registroRequest.getPassword()),
                registroRequest.getNombres(),
                registroRequest.getApellidos(),
                registroRequest.getRole());

        roleFieldsApplier.apply(usuario, RoleFieldsData.from(registroRequest));
        usuario.setDni(registroRequest.getDni());
        usuario.setTelefono(registroRequest.getTelefono());
        usuario.setDireccion(registroRequest.getDireccion());

        usuarioRepository.save(usuario);
        return success(AuthMessages.USER_REGISTERED);
    }

    public MessageResponse actualizarPerfil(String username, ActualizarPerfilRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return failure(AuthMessages.USER_NOT_FOUND);
        }
        if (request.isUpdatingPassword() && !request.isPasswordValid()) {
            return failure(AuthMessages.PASSWORDS_MISMATCH);
        }

        Usuario usuario = usuarioOpt.get();
        applyOptionalContactFields(usuario, request.getTelefono(), request.getDireccion());
        if (request.isUpdatingPassword()) {
            usuario.setPassword(encoder.encode(request.getPassword()));
        }
        markUpdated(usuario);
        usuarioRepository.save(usuario);
        return success(AuthMessages.PROFILE_UPDATED);
    }

    public MessageResponse cambiarPassword(String username, CambiarPasswordRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return failure(AuthMessages.USER_NOT_FOUND);
        }
        if (!request.isPasswordValid()) {
            return failure(AuthMessages.NEW_PASSWORDS_MISMATCH);
        }

        Usuario usuario = usuarioOpt.get();
        if (!encoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            return failure(AuthMessages.CURRENT_PASSWORD_INVALID);
        }
        if (encoder.matches(request.getNuevaPassword(), usuario.getPassword())) {
            return failure(AuthMessages.NEW_PASSWORD_SAME);
        }

        usuario.setPassword(encoder.encode(request.getNuevaPassword()));
        markUpdated(usuario);
        usuarioRepository.save(usuario);
        return success(AuthMessages.PASSWORD_CHANGED);
    }

    public UsuarioResponse getCurrentUser(String username) {

        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException(AuthMessages.USER_NOT_FOUND_PLAIN));
        return usuarioDtoMapper.toUsuarioResponse(usuario);
    }

    public List<UsuarioResponse> getUsuariosByRole(Role role) {
        return mapUsers(usuarioRepository.findByRoleAndActivoTrue(role));
    }

    public List<UsuarioResponse> getUsuariosByRoleIncluyendoInactivos(Role role) {
        return mapUsers(usuarioRepository.findByRole(role));
    }

    public List<UsuarioResponse> getAllUsuarios() {
        return mapUsers(usuarioRepository.findByActivoTrue());
    }

    public List<UsuarioResponse> getAllUsuariosIncluyendoInactivos() {
        return mapUsers(usuarioRepository.findAll());
    }

    public List<UsuarioResponse> buscarUsuariosPorNombre(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return getAllUsuarios();
        }
        return mapUsers(usuarioRepository.buscarPorNombre(texto.trim()));
    }

    public MessageResponse desactivarUsuario(Long id) {
        return setActivo(id, false, AuthMessages.USER_DEACTIVATED);
    }

    public MessageResponse activarUsuario(Long id) {
        return setActivo(id, true, AuthMessages.USER_ACTIVATED);
    }

    public MessageResponse actualizarUsuarioAdmin(Long id, ActualizarUsuarioAdminRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty()) {
            return failure(AuthMessages.USER_NOT_FOUND_PLAIN);
        }

        Usuario usuario = usuarioOpt.get();
        String uniquenessError = uniquenessValidator.validateAdminUsernameEmail(id, usuario, request);
        if (uniquenessError != null) {
            return failure(uniquenessError);
        }

        applyAdminBasicFields(usuario, request);
        roleFieldsApplier.apply(usuario, RoleFieldsData.from(request));
        if (request.isUpdatingPassword()) {
            usuario.setPassword(encoder.encode(request.getPassword()));
        }

        markUpdated(usuario);
        usuarioRepository.save(usuario);
        return success(AuthMessages.USER_UPDATED);
    }

    public MessageResponse getEstadisticas() {
        long totalUsuarios = usuarioRepository.count();
        long admins = usuarioRepository.countByRoleAndActivoTrue(Role.ADMIN);
        long docentes = usuarioRepository.countByRoleAndActivoTrue(Role.DOCENTE);
        long alumnos = usuarioRepository.countByRoleAndActivoTrue(Role.ALUMNO);
        long coordinadores = usuarioRepository.countByRoleAndActivoTrue(Role.COORDINADOR);
        long postulantes = usuarioRepository.countByRoleAndActivoTrue(Role.POSTULANTE);

        String estadisticas = String.format(
            "Total: %d, Admins: %d, Docentes: %d, Alumnos: %d, Coordinadores: %d, Postulantes: %d",
            totalUsuarios, admins, docentes, alumnos, coordinadores, postulantes
        );
        return success(estadisticas);
    }

    private MessageResponse setActivo(Long id, boolean activo, String successMessage) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return failure(AuthMessages.USER_NOT_FOUND_PLAIN);
        }
        Usuario usuario = usuarioOpt.get();
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
        return success(successMessage);
    }

    private void applyAdminBasicFields(Usuario usuario, ActualizarUsuarioAdminRequest request) {
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setRole(request.getRole());
        if (request.getDni() != null) {
            usuario.setDni(request.getDni());
        }
        applyOptionalContactFields(usuario, request.getTelefono(), request.getDireccion());
    }

    private void applyOptionalContactFields(Usuario usuario, String telefono, String direccion) {
        if (telefono != null) {
            usuario.setTelefono(telefono);
        }
        if (direccion != null) {
            usuario.setDireccion(direccion);
        }
    }

    private List<UsuarioResponse> mapUsers(List<Usuario> usuarios) {
        return usuarios.stream().map(usuarioDtoMapper::toUsuarioResponse).toList();
    }

    private void markUpdated(Usuario usuario) {
        usuario.setFechaActualizacion(AppClock.now());
    }

    private MessageResponse success(String message) {
        return new MessageResponse(message, true);
    }

    private MessageResponse failure(String message) {
        return new MessageResponse(message, false);
    }

}