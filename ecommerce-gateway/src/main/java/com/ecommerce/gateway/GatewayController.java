package com.ecommerce.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GatewayController {

    @Value("${services.user}")
    private String userService;

    @Value("${services.product}")
    private String productService;

    @Value("${services.order}")
    private String orderService;

    @Autowired
    private RestTemplate restTemplate;

    private String getTargetUrl(String path) {
        if (path.startsWith("/api/auth/")) {
            return userService + path;
        }
        if (path.startsWith("/api/admin/users")) {
            return userService + path;
        }
        if (path.startsWith("/api/admin/products")) {
            return productService + path;
        }
        if (path.startsWith("/api/admin/categories")) {
            return productService + path;
        }
        if (path.startsWith("/api/admin/promotions")) {
            return productService + path;
        }
        if (path.startsWith("/api/admin/product/")) {
            return productService + path;
        }
        if (path.startsWith("/api/admin/promotion/")) {
            return productService + path;
        }
        if (path.startsWith("/api/admin/site/")) {
            return productService + path;
        }
        if (path.startsWith("/api/admin/order/")) {
            return orderService + path;
        }
        if (path.startsWith("/api/admin/dashboard")) {
            return orderService + path;
        }
        if (path.startsWith("/api/admin/content/")) {
            return orderService + path;
        }
        if (path.startsWith("/api/products")) {
            return productService + path;
        }
        if (path.startsWith("/api/promotions")) {
            return productService + path;
        }
        if (path.startsWith("/api/client/product/")) {
            return productService + path;
        }
        if (path.startsWith("/api/client/promotion/")) {
            return productService + path;
        }
        if (path.startsWith("/api/client/cart/")) {
            return orderService + path;
        }
        if (path.startsWith("/api/client/order/")) {
            return orderService + path;
        }
        if (path.startsWith("/api/client/favorite/")) {
            return orderService + path;
        }
        if (path.startsWith("/api/client/review")) {
            return orderService + path;
        }
        if (path.startsWith("/api/client/feedback")) {
            return orderService + path;
        }
        if (path.startsWith("/api/client/logistics/")) {
            return orderService + path;
        }
        if (path.startsWith("/api/client/seckill/")) {
            return orderService + path;
        }
        if (path.startsWith("/api/admin/permission")) {
            return orderService + path;
        }
        return null;
    }

    private HttpHeaders copyHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if ("host".equalsIgnoreCase(headerName)) {
                continue;
            }
            if ("content-length".equalsIgnoreCase(headerName)) {
                continue;
            }
            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                headers.add(headerName, headerValues.nextElement());
            }
        }
        return headers;
    }

    private String buildQueryString(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            return "?" + queryString;
        }
        return "";
    }

    @RequestMapping("/**")
    public ResponseEntity<?> handleApiRequest(
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body,
            HttpMethod httpMethod) throws IOException {

        String path = request.getRequestURI();
        String targetUrl = getTargetUrl(path);
        if (targetUrl == null) {
            return ResponseEntity.notFound().build();
        }
        targetUrl += buildQueryString(request);

        HttpHeaders headers = copyHeaders(request);

        // Multipart uploads: @RequestBody can't deserialize them, so body is null.
        // Read raw bytes from InputStream to forward the file properly.
        if (body == null) {
            body = request.getInputStream().readAllBytes();
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    URI.create(targetUrl),
                    httpMethod,
                    entity,
                    byte[].class
            );

            HttpHeaders filteredHeaders = new HttpHeaders();
            response.getHeaders().forEach((name, values) -> {
                if ("transfer-encoding".equalsIgnoreCase(name)
                        || "content-length".equalsIgnoreCase(name)
                        || "connection".equalsIgnoreCase(name)) {
                    return;
                }
                values.forEach(v -> filteredHeaders.add(name, v));
            });

            return ResponseEntity.status(response.getStatusCode())
                    .headers(filteredHeaders)
                    .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("code", 503, "message", "Service temporarily unavailable"));
        }
    }
}