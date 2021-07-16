package com.op.ludo.security.facade;

import com.op.ludo.security.model.Credentials;
import com.op.ludo.security.model.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface IAuthenticationFacade {
    User getUser();

    Credentials getCredentials();

    Collection<? extends GrantedAuthority> getAuthorities();
}
