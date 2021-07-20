package com.op.ludo.auth.filter;

import com.google.firebase.auth.UserRecord;
import com.op.ludo.auth.Role;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class FirebasePrincipal implements FirebaseUserDetails {
  private final UserRecord userRecord;
  private final FirebaseTokenHolder token;

  public FirebasePrincipal(UserRecord userRecord, FirebaseTokenHolder token) {
    this.userRecord = userRecord;
    this.token = token;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Role principalRole;
    if (userRecord != null) {
      principalRole = stringToRole((String) userRecord.getCustomClaims().get("role"));
    } else {
      principalRole = stringToRole((String) token.getClaims().get("role"));
    }
    return Collections.singleton(new SimpleGrantedAuthority(principalRole.getAuthority()));
  }

  private Role stringToRole(String role) {
    return Role.getRole(role, Role.USER);
  }

  @Override
  public String getPassword() {
    return token.getEncodedToken();
  }

  @Override
  public String getUsername() {
    return userRecord != null ? userRecord.getDisplayName() : token.getName();
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
    return userRecord != null ? userRecord.getUid() : token.getUid();
  }
}
