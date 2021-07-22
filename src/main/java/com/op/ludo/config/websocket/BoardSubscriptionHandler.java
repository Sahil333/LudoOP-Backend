package com.op.ludo.config.websocket;

import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

@Slf4j
public class BoardSubscriptionHandler implements ChannelInterceptor {

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {

    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
    Principal principal = headerAccessor.getUser();

    log.info(
        "Msg to {} from {} : {}", headerAccessor.getDestination(), principal.getName(), message);
    if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
      log.info(
          "Subscribe request to {} from {}", headerAccessor.getDestination(), principal.getName());
      //            checkPermissions(principal, headerAccessor.getDestination());
    }

    return message;
  }
}
