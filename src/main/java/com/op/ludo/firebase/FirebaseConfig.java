package com.op.ludo.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Primary
    @Bean
    public void firebaseInit() {
        InputStream inputStream;
        try {
            inputStream = new ClassPathResource("firebase_config.json").getInputStream();
            FirebaseOptions options =
                    FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(inputStream))
                            .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            log.info("Firebase Initialized");
        } catch (IOException e) {
            log.info("Error initializing firebase", e);
        }
    }
}
