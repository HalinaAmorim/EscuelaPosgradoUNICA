package com.escuelaposgrado.Intranet.security.services;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.escuelaposgrado.Intranet.model.Usuario;
import com.escuelaposgrado.Intranet.security.role.RoleAuthorityMapper;

/**
 * Implementación de UserDetails para el sistema.
 */
public class UserPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(
            Long id,
            String username,
            String email,
            String password,
            Collection<? extends GrantedAuthority> authorities) {

        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserPrincipal create(
            Usuario usuario,
            RoleAuthorityMapper authorityMapper) {

        Collection<? extends GrantedAuthority> authorities =
                authorityMapper.mapAuthorities(usuario);

        return new UserPrincipal(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getEmail(),
                usuario.getPassword(),
                authorities
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}