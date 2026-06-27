package com.ecommerce.websocket;

import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class WebSocketContextInitializer {

    private final ApplicationContext applicationContext;

    public WebSocketContextInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        ChatWebSocket.setApplicationContext(applicationContext);
    }
}