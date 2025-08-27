package com.example.repository;

import com.example.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    RefreshToken findByToken(String token);

    void deleteByExpiresAtBefore(LocalDateTime expiresAt);

    void deleteByRevokedTrue();
}
