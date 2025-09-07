package com.example.config;

import com.example.filter.JwtAuthenticationFilter;
import com.example.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SecurityConfig {


    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints
                        .requestMatchers("/auth/register", "/auth/login", "/auth/refresh").permitAll()
                        .requestMatchers("/auth/me", "/auth/logout").hasAnyRole("USER", "ADMIN")
                        // Users endpoints
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        // Catalog endpoints
                        .requestMatchers(HttpMethod.GET, "/catalog/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/catalog/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/catalog/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/catalog/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/catalog/**").hasRole("ADMIN")
                        // Order endpoints
                        .requestMatchers(HttpMethod.GET, "/order/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/order/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/order/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/order/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/order/*/status").hasRole( "ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"path\": \"" + request.getRequestURI() + "\"}");
                }))
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
