package com.op.ludo.auth.facade;

import com.op.ludo.auth.filter.FirebasePrincipal;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface IAuthenticationFacade {
    FirebasePrincipal getPrincipal();

    Collection<? extends GrantedAuthority> getAuthorities();
}
