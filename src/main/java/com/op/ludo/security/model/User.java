package com.op.ludo.security.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class User {
    String uid;
    String name;
    String email;
    boolean isEmailVerified;
    String issuer;
    String picture;
}
