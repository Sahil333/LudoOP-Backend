package com.op.ludo.auth.facade;

import com.op.ludo.auth.filter.FirebasePrincipal;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityContextFacade implements IAuthenticationFacade {

    @Value("${firebase.auth.enabled}")
    boolean isAuthEnabled;

    public FirebasePrincipal getPrincipal() {
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

    public Boolean isContextSetForUser(String uid) {
        FirebasePrincipal principal = getPrincipal();
        return principal != null && principal.getUsername().equals(uid);
    }
}
