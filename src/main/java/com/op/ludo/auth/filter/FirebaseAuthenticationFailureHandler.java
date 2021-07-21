package com.op.ludo.auth.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

public class FirebaseAuthenticationFailureHandler {

    private final ObjectMapper mapper;

    public FirebaseAuthenticationFailureHandler() {
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(401);
        response.setContentType("application/json");
        response.getWriter().append(translateAuthException(exception, request));
    }

    private String translateAuthException(AuthenticationException exception, HttpServletRequest request) throws JsonProcessingException {
        AuthErrorResponse.AuthErrorResponseBuilder errorResponseBuilder = AuthErrorResponse.builder();
        errorResponseBuilder.dateTime(LocalDateTime.now());
        errorResponseBuilder.message(exception.getMessage());
        errorResponseBuilder.statusCode(401);
        errorResponseBuilder.path(request.getRequestURI());
        Throwable nestedException = exception.getCause();
        if(nestedException != null)
            errorResponseBuilder.details(nestedException.getMessage());

        return mapper.writeValueAsString(errorResponseBuilder.build());
    }

    @Getter
    @Setter
    @Builder
    private static class AuthErrorResponse {
        private String message;
        private String details;
        private int statusCode;
        private String path;
        private LocalDateTime dateTime;
    }
}
