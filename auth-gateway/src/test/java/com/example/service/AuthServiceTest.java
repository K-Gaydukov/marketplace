package com.example.service;

import com.example.dto.TokenResponse;
import com.example.entity.RefreshToken;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.exception.ValidationException;
import com.example.repository.RefreshTokenRepository;
import com.example.repository.UserRepository;
import com.example.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setMiddleName("");
        user.setRole(Role.ROLE_USER);
        user.setActive(true);

        refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(LocalDateTime.now());

        when(jwtUtil.getExpiration()).thenReturn(3600000L); // 1 час
        when(encoder.encode("password")).thenReturn("hashedPassword");
        when(jwtUtil.generateAccessToken("testuser", 1L, "User Test ", "ROLE_USER"))
                .thenReturn("access-token");
    }

    // register tests
    @Test
    void register_shouldRegisterUser() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(encoder.encode("password")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = authService.register(user);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPasswordHash()).isEqualTo("hashedPassword");
        assertThat(result.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(result.isActive()).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowValidationException_whenUsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(ValidationException.class, () -> authService.register(user));
    }

    // login tests
    @Test
    void login_shouldReturnTokenResponse() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(encoder.matches("password", "password")).thenReturn(true);
        when(jwtUtil.generateAccessToken("testuser", 1L, "Test User", "ROLE_USER")).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        TokenResponse result = authService.login("testuser", "password");

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getExpiresIn()).isEqualTo(3600000L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_shouldThrowValidationException_whenUserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(null);

        assertThrows(ValidationException.class, () -> authService.login("testuser", "password"));
    }

    @Test
    void login_shouldThrowValidationException_whenInvalidPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(encoder.matches("wrong-password", "hashedPassword")).thenReturn(false);

        assertThrows(ValidationException.class, () -> authService.login("testuser", "wrong-password"));
    }

    @Test
    void login_shouldThrowRuntimeException_whenUserNotActive() {
        user.setActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(encoder.matches("password", "hashedPassword")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.login("testuser", "password"));
    }

    // refresh tests
    @Test
    void refresh_shouldReturnNewTokenResponse() {
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(refreshToken);
        when(jwtUtil.generateAccessToken("testuser", 1L, "Test User", "ROLE_USER")).thenReturn("new-access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        TokenResponse result = authService.refresh("refresh-token");

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getExpiresIn()).isEqualTo(3600000L);
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class)); // Отзыв старого и создание нового
    }

    @Test
    void refresh_shouldThrowValidationException_whenTokenNotFound() {
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(null);

        assertThrows(ValidationException.class, () -> authService.refresh("refresh-token"));
    }

    @Test
    void refresh_shouldThrowValidationException_whenTokenRevoked() {
        refreshToken.setRevoked(true);
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(refreshToken);

        assertThrows(ValidationException.class, () -> authService.refresh("refresh-token"));
    }

    @Test
    void refresh_shouldThrowValidationException_whenTokenExpired() {
        refreshToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(refreshToken);

        assertThrows(ValidationException.class, () -> authService.refresh("refresh-token"));
    }

    // logout tests
    @Test
    void logout_shouldRevokeToken() {
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(refreshToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        authService.logout("refresh-token");

        assertThat(refreshToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void logout_shouldThrowValidationException_whenTokenNotFound() {
        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(null);

        assertThrows(ValidationException.class, () -> authService.logout("refresh-token"));
    }
}
