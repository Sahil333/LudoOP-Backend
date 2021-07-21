package com.op.ludo.auth.exceptions;

import org.springframework.security.authentication.BadCredentialsException;

public class FirebaseTokenInvalidException extends BadCredentialsException {

    public FirebaseTokenInvalidException(String msg) {
        super(msg);
    }

    public FirebaseTokenInvalidException(String msg, Exception ex) {
        super(msg, ex);
    }
}