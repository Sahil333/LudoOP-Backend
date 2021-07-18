package com.op.ludo.auth.filter;

import com.google.firebase.auth.UserRecord;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;


public class FirebasePrincipal implements FirebaseUserDetails {
    private final UserRecord userRecord;
    private final FirebaseTokenHolder token;

    public FirebasePrincipal(UserRecord userRecord, FirebaseTokenHolder token) {
        this.userRecord = userRecord;
        this.token = token;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Default return USER role. In future, use customClaims is userRecord/token
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return token.getEncodedToken();
    }

    @Override
    public String getUsername() {
        return userRecord != null ? userRecord.getDisplayName() : token.getVerifiedToken().getName();
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

    @Override
    public String getUid() {
        return userRecord != null ? userRecord.getUid() : token.getVerifiedToken().getUid();
    }
}
