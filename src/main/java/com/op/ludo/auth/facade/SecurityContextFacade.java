package com.op.ludo.auth.facade;

import com.op.ludo.auth.filter.FirebasePrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class SecurityContextFacade implements IAuthenticationFacade {

    public FirebasePrincipal getUser() {
        FirebasePrincipal userPrincipal = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Object principal = securityContext.getAuthentication().getPrincipal();
        if (principal instanceof FirebasePrincipal) {
            userPrincipal = ((FirebasePrincipal) principal);
        }
        return userPrincipal;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return securityContext.getAuthentication().getAuthorities();
    }
}
