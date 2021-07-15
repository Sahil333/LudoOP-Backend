package com.op.ludo.security.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.op.ludo.security.model.Credentials;
import com.op.ludo.security.model.User;
import com.op.ludo.security.utils.SecurityContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    SecurityContextUtil securityContextUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        verifyToken(request);
        filterChain.doFilter(request, response);
    }

    private void verifyToken(HttpServletRequest request) {
        FirebaseToken decodedToken = null;
        String token = securityContextUtil.getBearerToken(request);
        try {
            if (token != null && !token.equalsIgnoreCase("undefined")) {
                decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            }
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            log.error("Firebase Exception: {}", e.getLocalizedMessage());
        }
        User user = firebaseTokenToUserDto(decodedToken);
        if (user != null) {
            // TODO: set authorities for this authenticated user
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,
                    new Credentials(decodedToken, token), null);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    private User firebaseTokenToUserDto(FirebaseToken decodedToken) {
        User user = null;
        if (decodedToken != null) {
            user = User.builder()
                    .uid(decodedToken.getUid())
                    .name(decodedToken.getName())
                    .email(decodedToken.getEmail())
                    .picture(decodedToken.getPicture())
                    .issuer(decodedToken.getIssuer())
                    .isEmailVerified(decodedToken.isEmailVerified())
                    .build();
        }
        return user;
    }
}
