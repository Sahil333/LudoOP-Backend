package com.op.ludo.config.websocket;

import com.op.ludo.auth.Role;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

  @Override
  protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
    messages
        .nullDestMatcher()
        .authenticated()
        .simpDestMatchers("/app/**")
        .hasRole(Role.USER.toString())
        .simpSubscribeDestMatchers("/topic/game/**", "/queue/**", "/user/queue/**")
        .hasRole(Role.USER.toString())
        .simpMessageDestMatchers("/topic/**", "/queue/**", "/user/queue/**")
        .hasRole(Role.ADMIN.toString())
        .anyMessage()
        .denyAll();
  }

  @Override
  protected boolean sameOriginDisabled() {
    return true;
  }
}
