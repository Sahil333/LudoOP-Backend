package com.op.ludo.config.websocket;

import com.op.ludo.config.websocket.subscription.SubscriptionProvider;
import java.security.Principal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

@Slf4j
public class SubscriptionHandler implements ChannelInterceptor {

    private final List<SubscriptionProvider> subscriptionProviders;

    public SubscriptionHandler(List<SubscriptionProvider> subscriptionProviders) {
        this.subscriptionProviders = subscriptionProviders;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        Principal principal = headerAccessor.getUser();

        if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
            String destination = headerAccessor.getDestination();
            boolean permissionGranted = false;
            for (SubscriptionProvider provider : subscriptionProviders) {
                if (provider.hasPermission(principal, destination)) {
                    permissionGranted = true;
                    break;
                }
            }
            if (!permissionGranted) {
                throw new IllegalArgumentException(
                        "User="
                                + principal.getName()
                                + " don't have access to subscribe "
                                + destination);
            }
        }

        return message;
    }
}
