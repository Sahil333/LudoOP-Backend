package com.op.ludo.auth.filter;

import com.google.firebase.auth.FirebaseToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FirebaseTokenHolder {
    private final String encodedToken;
    private final FirebaseToken verifiedToken;
}