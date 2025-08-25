package com.example.filter;

import com.example.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer")) {
            token = token.substring(7);
            try {
                // Шаг 2: Проверяем токен
                Claims claims = jwtUtil.validateToken(token);

                // Шаг 3: Создаём UserDetails из claims
                UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                        claims.getSubject(),  // sub (username)
                        "",  // Пароль не нужен
                        List.of(new SimpleGrantedAuthority(claims.get("role", String.class))));  // Роль

                // Шаг 4: Устанавливаем аутентификацию
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                logger.error("Invalid token: " + e.getMessage() + "!");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
        // Шаг 5: Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }
}
