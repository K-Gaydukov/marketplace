package com.example.filter;

import com.example.exception.JwtValidationException;
import com.example.security.UserPrincipal;
import com.example.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Шаг 1: Извлекаем токен из заголовка
        String authHeaders = request.getHeader("Authorization");
        if (authHeaders != null && authHeaders.startsWith("Bearer ")) {
            String token = authHeaders.substring(7);
            try {
                // Проверка токена
                Claims claims = jwtUtil.validateToken(token);
                // Извлечение claims
                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                if (username != null && username.equals("order-service")) {
                    // Сервисный токен
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(role)));
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    // Пользовательский токен
                    Long userId = claims.get("uid", Long.class);
                    String fullName = claims.get("fio", String.class);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserPrincipal userPrincipal = new UserPrincipal(userId, username, fullName, role, token);
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                userPrincipal.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (JwtValidationException e) {
                throw e;  // Пробрасываем в GlobalExceptionHandler
            }
        }
        // Шаг 5: Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }
}
