package com.escuelaposgrado.Intranet.security.role;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.escuelaposgrado.Intranet.model.Usuario;

@Component
public class RoleAuthorityMapper {

    public Collection<GrantedAuthority> mapAuthorities(Usuario usuario) {

        return Collections.singletonList(
                new SimpleGrantedAuthority(
                        "ROLE_" + usuario.getRol().name()
                )
        );
    }
}