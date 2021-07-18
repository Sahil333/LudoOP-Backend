package com.op.ludo.auth.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.op.ludo.auth.exceptions.FirebaseTokenInvalidException;
import com.op.ludo.auth.exceptions.FirebaseUserNotExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private final boolean fetchUserRecord;
    private final FirebaseAuthenticationFailureHandler authenticationFailureHandler;

    public FirebaseTokenFilter(boolean fetchUserRecord) {
        this.fetchUserRecord = fetchUserRecord;
        this.authenticationFailureHandler = new FirebaseAuthenticationFailureHandler();
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String token = getBearerToken(request);
        if (token != null && !token.equalsIgnoreCase("undefined")) {
            UserRecord record = null;
            FirebaseTokenHolder tokenHolder;
            try {
                FirebaseToken verifiedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                if(fetchUserRecord) {
                    record = FirebaseAuth.getInstance().getUser(verifiedToken.getUid());
                }
                tokenHolder = new FirebaseTokenHolder(token, verifiedToken);
            } catch (IllegalArgumentException e) {
                throw new FirebaseTokenInvalidException("Illegal token provided", e);
            } catch (FirebaseAuthException e) {
                switch (e.getAuthErrorCode()) {
                    case USER_NOT_FOUND:
                        throw new FirebaseUserNotExistsException("User not found", e);
                    case INVALID_ID_TOKEN:
                        throw new FirebaseTokenInvalidException("Invalid id token", e);
                    case EXPIRED_ID_TOKEN:
                        throw new FirebaseTokenInvalidException("Expired id token", e);
                    case REVOKED_ID_TOKEN:
                        throw new FirebaseTokenInvalidException("Id token has been revoked", e);
                    default:
                        throw new AuthenticationServiceException("Failed to get user", e);
                }
            }

            FirebasePrincipal principal = new FirebasePrincipal(record, tokenHolder);
            FirebaseAuthenticationToken authenticationToken =
                    new FirebaseAuthenticationToken(principal, tokenHolder, principal.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            log.info("authenticated");
            return authenticationToken;
        } else {
            throw new FirebaseTokenInvalidException("Authorization token is not available in headers");
        }
    }

    private String getBearerToken(HttpServletRequest request) {
        String bearerToken = null;
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            bearerToken = authorization.substring(7);
        }
        return bearerToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authenticationResult = attemptAuthentication(request, response);
            SecurityContextHolder.getContext().setAuthentication(authenticationResult);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, ex);
        }
    }
}
