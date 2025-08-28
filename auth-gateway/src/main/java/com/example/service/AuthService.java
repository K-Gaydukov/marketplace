package com.example.service;

import com.example.dto.TokenResponse;
import com.example.entity.RefreshToken;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.RefreshTokenRepository;
import com.example.repository.UserRepository;
import com.example.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder encoder,
                       JwtUtil jwtUtil,
                       RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public User register(User user) {
        user.setPasswordHash(encoder.encode(user.getPasswordHash()));  // Хешируем пароль
//        user.setRole(Role.ROLE_USER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public TokenResponse login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !encoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        if (!user.isActive()) {
            throw new RuntimeException("User not active");
        }
        String accessToken = jwtUtil.generateAccessToken(
                username,
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getRole().name());

        String refreshToken = generateRefreshToken(user); // Метод ниже

        return new TokenResponse(accessToken, refreshToken, "Bearer", jwtUtil.getExpiration());
    }

    private String generateRefreshToken(User user) {
        String token = UUID.randomUUID().toString(); // Или JWT
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(token);
        rt.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 дней
        rt.setRevoked(false);
        rt.setCreatedAt(LocalDateTime.now());
        refreshTokenRepository.save(rt);
        return token;
    }

    public TokenResponse refresh(String refreshToken) {
        // Шаг 1: Проверяем refresh-токен
        RefreshToken rt = refreshTokenRepository.findByToken(refreshToken);
        if (rt == null || rt.isRevoked() || rt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid refresh token");
        }
        // Шаг 2: Находим пользователя
        User user = rt.getUser();
        // Шаг 3: Генерируем новый access-токен
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getUsername(),
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getRole().name());
        // Шаг 4: Отзываем старый refresh-токен и создаём новый
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);
        String newRefreshToken = generateRefreshToken(user);  // новый refresh
        // Шаг 5: Возвращаем новый access и refresh
        return new TokenResponse(newAccessToken, newRefreshToken, "Bearer", jwtUtil.getExpiration());
    }

    public void logout(String refreshToken) {
        RefreshToken rt = refreshTokenRepository.findByToken(refreshToken);
        if (rt != null) {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        }
    }
}
