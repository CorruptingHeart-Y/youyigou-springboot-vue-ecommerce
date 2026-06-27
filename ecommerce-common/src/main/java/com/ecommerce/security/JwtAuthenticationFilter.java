package com.ecommerce.security;

import com.ecommerce.entity.Permission;
import com.ecommerce.entity.RolePermission;
import com.ecommerce.mapper.PermissionMapper;
import com.ecommerce.mapper.RolePermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String bearerToken = request.getHeader("Authorization");
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = bearerToken.substring(7);
            Claims claims = jwtUtil.parseToken(token);
            Long userId = jwtUtil.getUserId(claims);
            String role = jwtUtil.getRole(claims);
            String username = claims.getSubject();

            // Check if token is still valid in Redis (not invalidated by logout)
            try {
                Object cached = redisTemplate.opsForValue().get("user:token:" + userId);
                if (cached == null || !token.equals(cached.toString())) {
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                // Redis unavailable, allow token
            }

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

            // Load dynamic permissions for the user's role
            try {
                List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRole, role));
                if (!rolePermissions.isEmpty()) {
                    List<Long> permIds = rolePermissions.stream()
                            .map(RolePermission::getPermissionId).collect(Collectors.toList());
                    List<Permission> perms = permissionMapper.selectBatchIds(permIds);
                    for (Permission p : perms) {
                        if (p != null) {
                            authorities.add(new SimpleGrantedAuthority(p.getCode()));
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to load permissions: {}", e.getMessage());
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, userId, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.debug("JWT验证失败: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}