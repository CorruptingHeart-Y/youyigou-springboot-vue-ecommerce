package com.ecommerce.gateway;

import jakarta.annotation.PostConstruct;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

/**
 * Gateway-side WebSocket proxy endpoint.
 *
 * Accepts client WebSocket connections at /ws/chat/{token} on the gateway (:8080)
 * and transparently relays them to the user-service ChatWebSocket (:8081).
 *
 * The proxy does NOT decode or validate the token — the backend handles auth.
 * If the backend rejects the token, the backend closes the connection and the
 * proxy propagates the close to the client.
 */
@Slf4j
@Component
@ServerEndpoint("/ws/chat/{token}")
public class WebSocketProxyServer {

    /** WebSocket URL of the user service, e.g. ws://localhost:8081 */
    private static String backendBase;

    @Value("${services.user}")
    public void setServicesUser(String httpUrl) {
        // Convert http://host:port → ws://host:port
        WebSocketProxyServer.backendBase = httpUrl.replaceFirst("^http://", "ws://").replaceFirst("^https://", "wss://");
    }

    private Session backendSession;
    private Session clientSession;

    @OnOpen
    public void onOpen(Session clientSession, @PathParam("token") String token) {
        this.clientSession = clientSession;

        try {
            String backendUrl = backendBase + "/ws/chat/" + token;
            log.info("Proxy opening backend connection to {}", backendUrl);

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            Session bs = container.connectToServer(new BackendEndpoint(this), URI.create(backendUrl));
            this.backendSession = bs;
            log.info("Proxy connected to backend for client session {}", clientSession.getId());
        } catch (Exception e) {
            log.error("Proxy failed to connect to backend: {}", e.getMessage());
            try {
                clientSession.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Backend unreachable"));
            } catch (IOException ignored) {}
        }
    }

    @OnMessage
    public void onMessage(String message) {
        if (backendSession != null && backendSession.isOpen()) {
            backendSession.getAsyncRemote().sendText(message);
        }
    }

    @OnClose
    public void onClose() {
        closeBackend();
    }

    @OnError
    public void onError(Throwable error) {
        log.error("Proxy error", error);
        closeBackend();
    }

    private void closeBackend() {
        if (backendSession != null && backendSession.isOpen()) {
            try { backendSession.close(); } catch (IOException ignored) {}
        }
    }

    /** Receives messages from the backend and forwards to the client. */
    @ClientEndpoint
    public static class BackendEndpoint {

        private final WebSocketProxyServer proxy;

        public BackendEndpoint() {
            this.proxy = null; // required by Jakarta WebSocket client API
        }

        public BackendEndpoint(WebSocketProxyServer proxy) {
            this.proxy = proxy;
        }

        @OnMessage
        public void onMessage(String message) {
            if (proxy != null && proxy.clientSession != null && proxy.clientSession.isOpen()) {
                proxy.clientSession.getAsyncRemote().sendText(message);
            }
        }

        @OnClose
        public void onClose() {
            if (proxy != null && proxy.clientSession != null && proxy.clientSession.isOpen()) {
                try { proxy.clientSession.close(); } catch (IOException ignored) {}
            }
        }

        @OnError
        public void onError(Throwable error) {
            log.error("Backend connection error", error);
        }
    }
}
