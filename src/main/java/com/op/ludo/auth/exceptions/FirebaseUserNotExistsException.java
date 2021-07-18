package com.op.ludo.auth.exceptions;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

public class FirebaseUserNotExistsException extends AuthenticationCredentialsNotFoundException {

    public FirebaseUserNotExistsException(String msg, Exception e) {
        super(msg, e);
    }
}