package com.op.ludo.security.model;

import com.google.firebase.auth.FirebaseToken;
import lombok.Value;

@Value
public class Credentials {
    FirebaseToken decodedToken;
    String idToken;
}
