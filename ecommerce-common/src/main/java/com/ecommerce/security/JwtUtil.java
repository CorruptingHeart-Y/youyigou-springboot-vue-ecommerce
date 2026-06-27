package com.ecommerce.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    public JwtUtil(@Value("${jwt.secret:ecommerce-platform-secret-key-min-256-bits-length!!}") String secret,
                   @Value("${jwt.expiration:86400000}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(Long userId, String username, String role) {
        return generateToken(userId, username, role, expiration);
    }

    public String generateToken(Long userId, String username, String role, long customExpiration) {
        Date now = new Date();
        return Jwts.builder()
                .claims(Map.of("userId", userId, "role", role))
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + customExpiration))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT已过期");
            throw e;
        } catch (JwtException e) {
            log.warn("JWT解析失败: {}", e.getMessage());
            throw e;
        }
    }

    public Long getUserId(Claims claims) {
        return claims.get("userId", Long.class);
    }

    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }
}