package com.op.ludo.auth.filter;

import com.google.firebase.auth.FirebaseToken;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FirebaseTokenHolder {
  @Getter private final String encodedToken;
  private final FirebaseToken decodedToken;

  public Map<String, Object> getClaims() {
    return this.decodedToken.getClaims();
  }

  public String getUid() {
    return this.decodedToken.getUid();
  }

  public String getName() {
    return this.decodedToken.getName();
  }
}
