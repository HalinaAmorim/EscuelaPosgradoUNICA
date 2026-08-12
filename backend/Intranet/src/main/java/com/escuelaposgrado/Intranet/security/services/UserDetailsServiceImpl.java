package com.escuelaposgrado.Intranet.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.escuelaposgrado.Intranet.model.Usuario;
import com.escuelaposgrado.Intranet.repository.UsuarioRepository;
import com.escuelaposgrado.Intranet.security.role.RoleAuthorityMapper;

/**
 * Implementación personalizada de UserDetailsService.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleAuthorityMapper roleAuthorityMapper;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Usuario usuario = buscarUsuarioAtivoPorEmail(email);

        return UserPrincipal.create(
                usuario,
                roleAuthorityMapper);
    }

    /**
     * Busca um usuário ativo pelo email.
     */
    private Usuario buscarUsuarioAtivoPorEmail(String email)
            throws UsernameNotFoundException {

        return usuarioRepository
                .findByEmailAndActivoTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + email));
    }
}