package com.op.ludo.config;

import com.op.ludo.auth.Role;
import com.op.ludo.auth.filter.FirebaseTokenFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;

@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER - 10)
public class HttpSecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${firebase.auth.enabled}")
  private Boolean authEnabled;

  @Value("${firebase.auth.fetch-user}")
  private Boolean fetchUser;

  @Bean
  public FilterRegistrationBean<FirebaseTokenFilter> registration() throws Exception {
    FirebaseTokenFilter tokenFilter = new FirebaseTokenFilter(fetchUser);
    FilterRegistrationBean<FirebaseTokenFilter> registration =
        new FilterRegistrationBean<>(tokenFilter);
    registration.setEnabled(false);
    return registration;
  }

  @Override
  public void configure(WebSecurity web) {
    web.ignoring()
        .antMatchers(
            "/v2/api-docs",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/v2/swagger.json");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    if (authEnabled) {
      http.antMatcher("/v1/**")
          .addFilterAfter(registration().getFilter(), RequestCacheAwareFilter.class)
          .authorizeRequests()
          .antMatchers("/v1/lobby/**")
          .hasRole(Role.USER.toString())
          .antMatchers("/v1/join/**")
          .hasRole(Role.USER.toString())
          //                        .antMatchers("/health/**").hasRole(Roles.ADMIN)
          .antMatchers("/**")
          .denyAll()
          .and()
          .csrf()
          .disable()
          .anonymous()
          .authorities(Role.ANONYMOUS.getAuthority());
    } else {
      http.csrf().disable().authorizeRequests().antMatchers("/**").permitAll();
    }
  }
}
