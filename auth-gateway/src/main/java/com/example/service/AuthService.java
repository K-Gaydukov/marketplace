package com.example.service;

import com.example.entity.RefreshToken;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.RefreshTokenRepository;
import com.example.repository.UserRepository;
import com.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;


    public User register(User user) {
        user.setPasswordHash(encoder.encode(user.getPasswordHash()));  // Хешируем пароль
        user.setRole(Role.ROLE_USER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public Map<String, String> login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !encoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        String accessToken = jwtUtil.generateAccessToken(
                username,
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getRole().name());

        String refreshToken = generateRefreshToken(user.getId()); // Метод ниже
        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    private String generateRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString(); // Или JWT
        RefreshToken rt = new RefreshToken();
        rt.setId(userId);
        rt.setToken(token);
        rt.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 дней
        rt.setRevoked(false);
        rt.setCreatedAt(LocalDateTime.now());
        refreshTokenRepository.save(rt);
        return token;
    }

}
