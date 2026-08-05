package com.escuelaposgrado.Intranet.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Intranet.dto.UsuarioDTO;
import com.escuelaposgrado.Intranet.model.Role;
import com.escuelaposgrado.Intranet.model.Usuario;
import com.escuelaposgrado.Intranet.repository.UsuarioRepository;
import com.escuelaposgrado.Intranet.service.exception.CodigoJaExisteException;
import com.escuelaposgrado.Intranet.service.exception.EmailJaExisteException;
import com.escuelaposgrado.Intranet.service.exception.UsuarioNotFoundException;

@Service
@Transactional
public class UsuarioService {

    private static final ZoneId ZONE_ID =
            ZoneId.of("America/Fortaleza");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Obter todos os usuários ativos.
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    /**
     * Obter usuário por ID como DTO.
     */
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerUsuarioPorId(Long id) {
        return convertirADTO(buscarUsuario(id));
    }

    /**
     * Obter usuário por username.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Obter usuário por email como DTO.
     */
    @Transactional(readOnly = true)
    public UsuarioDTO obtenerUsuarioPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsuarioNotFoundException(
                                "Usuário não encontrado"));

        return convertirADTO(usuario);
    }

    /**
     * Obter usuários por role.
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> obtenerUsuariosPorRol(String rol) {

        Role role = Role.valueOf(rol.toUpperCase());

        return usuarioRepository
                .findByRoleAndActivoTrue(role)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    /**
     * Obter usuários paginados.
     */
    @Transactional(readOnly = true)
    public Page<UsuarioDTO> obtenerUsuariosPaginados(
            Pageable pageable) {

        Page<Usuario> usuarios =
                usuarioRepository.findByEliminadoFalse(pageable);

        return usuarios.map(this::convertirADTO);
    }

    /**
     * Criar usuário.
     */
    public UsuarioDTO crearUsuario(UsuarioDTO usuarioDTO) {

        validarCriacao(usuarioDTO);

        Usuario usuario = new Usuario();

        usuario.setNombres(usuarioDTO.getNombres());
        usuario.setApellidos(usuarioDTO.getApellidos());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setCodigo(usuarioDTO.getCodigo());
        usuario.setTelefono(usuarioDTO.getTelefono());
        usuario.setDireccion(usuarioDTO.getDireccion());
        usuario.setRol(Role.valueOf(
                usuarioDTO.getRol().toUpperCase()));

        usuario.setActivo(
                usuarioDTO.getActivo() != null
                        ? usuarioDTO.getActivo()
                        : true
        );

        definirSenha(usuario, usuarioDTO);

        usuario = usuarioRepository.save(usuario);

        return convertirADTO(usuario);
    }

    /**
     * Atualizar usuário.
     */
    public UsuarioDTO actualizarUsuario(
            Long id,
            UsuarioDTO usuarioDTO) {

        Usuario usuario = buscarUsuario(id);

        validarAtualizacao(id, usuarioDTO);

        atualizarDados(usuario, usuarioDTO);

        usuario = usuarioRepository.save(usuario);

        return convertirADTO(usuario);
    }

    /**
     * Validar dados de criação.
     */
    private void validarCriacao(UsuarioDTO usuarioDTO) {

        if (usuarioRepository.existsByEmail(
                usuarioDTO.getEmail())) {

            throw new EmailJaExisteException(
                    usuarioDTO.getEmail());
        }

        if (usuarioRepository.existsByCodigo(
                usuarioDTO.getCodigo())) {

            throw new CodigoJaExisteException(
                    usuarioDTO.getCodigo());
        }
    }

    /**
     * Validar dados de atualização.
     */
    private void validarAtualizacao(
            Long id,
            UsuarioDTO usuarioDTO) {

        Usuario usuario = buscarUsuario(id);

        if (!usuario.getEmail().equals(usuarioDTO.getEmail())
                && usuarioRepository.existsByEmail(
                        usuarioDTO.getEmail())) {

            throw new EmailJaExisteException(
                    usuarioDTO.getEmail());
        }

        if (!usuario.getCodigo().equals(usuarioDTO.getCodigo())
                && usuarioRepository.existsByCodigo(
                        usuarioDTO.getCodigo())) {

            throw new CodigoJaExisteException(
                    usuarioDTO.getCodigo());
        }
    }

    /**
     * Atualizar os dados permitidos do usuário.
     */
    private void atualizarDados(
            Usuario usuario,
            UsuarioDTO usuarioDTO) {

        usuario.setNombres(usuarioDTO.getNombres());
        usuario.setApellidos(usuarioDTO.getApellidos());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setCodigo(usuarioDTO.getCodigo());
        usuario.setTelefono(usuarioDTO.getTelefono());
        usuario.setDireccion(usuarioDTO.getDireccion());

        usuario.setRol(
                Role.valueOf(
                        usuarioDTO.getRol().toUpperCase()
                )
        );

        usuario.setActivo(usuarioDTO.getActivo());
    }

    /**
     * Definir senha do usuário.
     */
    private void definirSenha(
            Usuario usuario,
            UsuarioDTO usuarioDTO) {

        String senha = usuarioDTO.getPassword();

        if (senha != null && !senha.isEmpty()) {
            usuario.setPassword(
                    passwordEncoder.encode(senha)
            );
            return;
        }

        usuario.setPassword(
                passwordEncoder.encode(
                        usuarioDTO.getCodigo()
                )
        );
    }

    /**
     * Buscar usuário ou lançar exceção.
     */
    private Usuario buscarUsuario(Long id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new UsuarioNotFoundException(
                                "Usuário não encontrado com ID: " + id
                        )
                );
    }

    /**
     * Eliminar usuário logicamente.
     */
    public void eliminarUsuario(Long id) {

        Usuario usuario = buscarUsuario(id);

        usuario.setEliminado(true);

        usuarioRepository.save(usuario);
    }

    /**
     * Desativar usuário.
     */
    public void desactivarUsuario(Long id) {

        Usuario usuario = buscarUsuario(id);

        usuario.setActivo(false);

        usuarioRepository.save(usuario);
    }

    /**
     * Reativar usuário.
     */
    public void reactivarUsuario(Long id) {

        Usuario usuario = buscarUsuario(id);

        usuario.setActivo(true);

        usuarioRepository.save(usuario);
    }

    /**
     * Alterar estado do usuário.
     */
    public void cambiarEstadoUsuario(
            Long id,
            Boolean activo) {

        Usuario usuario = buscarUsuario(id);

        usuario.setActivo(activo);

        usuarioRepository.save(usuario);
    }

    /**
     * Atualizar último acesso.
     */
    public void actualizarUltimoAcceso(Long id) {

        LocalDateTime agora =
                LocalDateTime.now(ZONE_ID);

        usuarioRepository.actualizarUltimoAcceso(
                id,
                agora
        );
    }

    /**
     * Obter usuários recentes.
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosRecientes() {
        return usuarioRepository.findUsuariosRecientes();
    }

    /**
     * Obter usuários com acesso recente.
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosConAccesoReciente(
            int dias) {

        LocalDateTime dataInicial =
                LocalDateTime.now(ZONE_ID)
                        .minusDays(dias);

        return usuarioRepository
                .findUsuariosConAccesoDesde(dataInicial);
    }

    /**
     * Contar usuários por role.
     */
    @Transactional(readOnly = true)
    public long contarUsuariosPorRol(Role role) {
        return usuarioRepository
                .countByRoleAndActivoTrue(role);
    }

    /**
     * Verificar disponibilidade do username.
     */
    @Transactional(readOnly = true)
    public boolean isUsernameDisponible(
            String username) {

        return !usuarioRepository.existsByUsername(username);
    }

    /**
     * Verificar disponibilidade do email.
     */
    @Transactional(readOnly = true)
    public boolean isEmailDisponible(String email) {

        return !usuarioRepository.existsByEmail(email);
    }

    /**
     * Verificar disponibilidade do DNI.
     */
    @Transactional(readOnly = true)
    public boolean isDniDisponible(String dni) {

        return !usuarioRepository.existsByDni(dni);
    }

    /**
     * Obter estudante por código.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerEstudiantePorCodigo(
            String codigo) {

        return usuarioRepository.findByCodigoEstudiante(codigo);
    }

    /**
     * Obter docente por código.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerDocentePorCodigo(
            String codigo) {

        return usuarioRepository.findByCodigoDocente(codigo);
    }

    /**
     * Buscar usuários por texto.
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> buscarUsuarios(String termino) {

        return usuarioRepository
                .buscarPorNombreOEmail(termino)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    /**
     * Obter estudantes como DTO.
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> obtenerEstudiantes() {

        return obtenerUsuariosPorRol("ALUMNO");
    }

    /**
     * Obter docentes como DTO.
     */
    @Transactional(readOnly = true)
    public List<UsuarioDTO> obtenerDocentes() {

        return obtenerUsuariosPorRol("DOCENTE");
    }

    /**
     * Alterar senha.
     */
    public void cambiarPassword(
            Long id,
            String passwordActual,
            String passwordNuevo) {

        Usuario usuario = buscarUsuario(id);

        if (!passwordEncoder.matches(
                passwordActual,
                usuario.getPassword())) {

            throw new IllegalArgumentException(
                    "A senha atual está incorreta"
            );
        }

        usuario.setPassword(
                passwordEncoder.encode(passwordNuevo)
        );

        usuarioRepository.save(usuario);
    }

    /**
     * Verificar se é o usuário atual.
     */
    @Transactional(readOnly = true)
    public boolean esUsuarioActual(
            Long id,
            String email) {

        Optional<Usuario> usuario =
                usuarioRepository.findByEmail(email);

        return usuario.isPresent()
                && usuario.get().getId().equals(id);
    }

    /**
     * Verificar se email é único.
     */
    @Transactional(readOnly = true)
    public boolean esEmailUnico(String email) {
        return !usuarioRepository.existsByEmail(email);
    }

    /**
     * Verificar se código é único.
     */
    @Transactional(readOnly = true)
    public boolean esCodigoUnico(String codigo) {
        return !usuarioRepository.existsByCodigo(codigo);
    }

    /**
     * Converter entidade para DTO.
     */
    private UsuarioDTO convertirADTO(Usuario usuario) {

        UsuarioDTO dto = new UsuarioDTO();

        dto.setId(usuario.getId());
        dto.setNombres(usuario.getNombres());
        dto.setApellidos(usuario.getApellidos());
        dto.setEmail(usuario.getEmail());
        dto.setCodigo(usuario.getCodigo());
        dto.setTelefono(usuario.getTelefono());
        dto.setDireccion(usuario.getDireccion());
        dto.setRol(usuario.getRol().name());
        dto.setActivo(usuario.getActivo());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setFechaRegistro(usuario.getFechaRegistro());
        dto.setFechaActualizacion(
                usuario.getFechaActualizacion()
        );
        dto.setUltimoAcceso(usuario.getUltimoAcceso());
        dto.setEliminado(usuario.getEliminado());

        return dto;
    }
}