package com.op.ludo.config;

import com.op.ludo.auth.filter.FirebaseTokenFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;

@Configuration
public class SecurityConfig {
    public static class Roles {
        public static final String ANONYMOUS = "ANONYMOUS";
        public static final String USER = "USER";
        static public final String ADMIN = "ADMIN";

        private static final String ROLE_ = "ROLE_";
        public static final String ROLE_ANONYMOUS = ROLE_ + ANONYMOUS;
        public static final String ROLE_USER = ROLE_ + USER;
        static public final String ROLE_ADMIN = ROLE_ + ADMIN;
    }

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER-10)
    protected static class ApplicationSecurity extends WebSecurityConfigurerAdapter {

        @Value("${firebase.auth.enabled}")
        private Boolean firebaseEnabled;

        @Value("${firebase.auth.fetch-user}")
        private Boolean fetchUser;

        @Bean
        public FilterRegistrationBean<FirebaseTokenFilter> registration() throws Exception {
            FirebaseTokenFilter tokenFilter = new FirebaseTokenFilter(fetchUser);
            FilterRegistrationBean<FirebaseTokenFilter> registration = new FilterRegistrationBean<>(tokenFilter);
            registration.setEnabled(false);
            return registration;
        }

        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**",
                    "/configuration/security", "/swagger-ui.html", "/webjars/**", "/v2/swagger.json");
        }

        // TODO: configure endpoints security according to our needs
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            if (firebaseEnabled) {
                http.addFilterAfter(registration().getFilter(), RequestCacheAwareFilter.class).authorizeRequests()//
                        .antMatchers("/api/open/**").hasAnyRole(Roles.ANONYMOUS)//
                        .antMatchers("/api/client/**").hasRole(Roles.USER)//
                        .antMatchers("/api/admin/**").hasRole(Roles.ADMIN)//
                        .antMatchers("/health/**").hasRole(Roles.ADMIN)//
                        .antMatchers("/**").denyAll()//
                        .and().csrf().disable()//
                        .anonymous().authorities(Roles.ROLE_ANONYMOUS);//
            } else {
                http.httpBasic().and().authorizeRequests()//
                        .antMatchers("/api/open/**").hasAnyRole(Roles.ANONYMOUS)//
                        .antMatchers("/api/client/**").hasRole(Roles.USER)//
                        .antMatchers("/api/admin/**").hasRole(Roles.ADMIN)//
                        .antMatchers("/health/**").hasRole(Roles.ADMIN)//
                        .antMatchers("/**").denyAll()//
                        .and().csrf().disable()//
                        .anonymous().authorities(Roles.ROLE_ANONYMOUS);//
            }
        }

        @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
        @Override
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }
    }
}
