package com.ecommerce.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

    /**
     * Register JSR-356 @ServerEndpoint beans with the servlet container.
     * Disabled on the gateway — the gateway uses its own proxy endpoint instead.
     */
    @Bean
    @ConditionalOnExpression("'${spring.application.name}' != 'api-gateway'")
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}