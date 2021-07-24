package com.op.ludo.auth.filter;

import org.springframework.security.core.userdetails.UserDetails;

public interface FirebaseUserDetails extends UserDetails {
    String getDisplayName();
}
