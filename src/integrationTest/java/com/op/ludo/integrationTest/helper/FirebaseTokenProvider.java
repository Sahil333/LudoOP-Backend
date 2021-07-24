package com.op.ludo.integrationTest.helper;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class FirebaseTokenProvider {

  private final String firebaseTokenEndpoint =
      "https://www.googleapis.com/identitytoolkit/v3/relyingparty/verifyPassword?key=%s";

  @Autowired TestRestTemplate restTemplate;

  public Map<String, String> getToken(String email, String password) {
    String APIKey = DataReader.getFirebaseAPIKey();
    Map<String, Object> credentials = new HashMap<>();
    credentials.put("email", email);
    credentials.put("password", password);
    credentials.put("returnSecureToken", true);
    String endpoint = String.format(firebaseTokenEndpoint, APIKey);
    return restTemplate
        .exchange(
            endpoint,
            HttpMethod.POST,
            new HttpEntity<>(credentials),
            new ParameterizedTypeReference<Map<String, String>>() {})
        .getBody();
  }
}
