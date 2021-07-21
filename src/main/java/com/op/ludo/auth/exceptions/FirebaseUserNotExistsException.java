package com.op.ludo.auth.exceptions;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class FirebaseUserNotExistsException extends UsernameNotFoundException {

  public FirebaseUserNotExistsException(String msg, Exception e) {
    super(msg, e);
  }
}
