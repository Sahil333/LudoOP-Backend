package com.op.ludo.integrationTest.helper;

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

  public String getToken() {
    String APIKey = DataReader.getFirebaseAPIKey();
    Map<String, String> credentials = DataReader.getCredentials1();
    String endpoint = String.format(firebaseTokenEndpoint, APIKey);
    return restTemplate
        .exchange(
            endpoint,
            HttpMethod.POST,
            new HttpEntity<>(credentials),
            new ParameterizedTypeReference<Map<String, String>>() {})
        .getBody()
        .get("idToken");
  }
}
