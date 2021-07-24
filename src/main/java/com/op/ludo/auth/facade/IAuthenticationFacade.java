package com.op.ludo.auth.facade;

import com.op.ludo.auth.filter.FirebasePrincipal;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public interface IAuthenticationFacade {
    FirebasePrincipal getPrincipal();

    Collection<? extends GrantedAuthority> getAuthorities();

    Boolean isContextSetForUser(String uid);
}
