package com.op.ludo.config.websocket;

import com.op.ludo.config.websocket.subscription.BoardSubscriptionProvider;
import com.op.ludo.config.websocket.subscription.SubscriptionProvider;
import com.op.ludo.config.websocket.subscription.UserErrorQueueProvider;
import com.op.ludo.service.LobbyService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired private LobbyService lobbyService;

    @Bean
    public List<SubscriptionProvider> subscriptionProviders() {
        List<SubscriptionProvider> providers = new ArrayList<>();
        providers.add(new BoardSubscriptionProvider(lobbyService));
        providers.add(new UserErrorQueueProvider());
        return providers;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("v1/join");
        registry.addEndpoint("v1/join").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new SubscriptionHandler(subscriptionProviders()));
    }
}
