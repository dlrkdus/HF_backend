package com.hf.healthfriend.global.config.websocket;

import com.hf.healthfriend.global.websocket.CustomWebSocketHandshakeHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class SimpleBrokerWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${client.origin}")
    private String clientOrigin;
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/hf/portfolio")
                .setAllowedOrigins(this.clientOrigin)
                .setHandshakeHandler(new CustomWebSocketHandshakeHandler());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/hf/app");

        // 내장 메시지 브로커
        registry.enableSimpleBroker("/hf/topic", "/hf/queue", "/hf/user");

        registry.setUserDestinationPrefix("/hf/user");
    }
}
