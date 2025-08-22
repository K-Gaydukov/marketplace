package com.example.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.expiration}")
    private Long expiration;        // Время жизни токена (application.properties)

    @Value("${jwt.issuer}")
    private String issuer;          // Имя создателя токена (н-р: marketplace)

    private final PrivateKey privateKey; // Приватный ключ для подписи
    private final PublicKey publicKey;  // Публичный ключ для проверки

    // Конструктор для ключей
    public JwtUtil(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    // Генерация токена с claims(поля)
    public String generateAccessToken(String sub, Long uid, String fio, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(sub)  // sub
                .claim("uid", uid)  // uid
                .claim("fio", fio)  // fio
                .claim("role", role)  // role
                .issuer(issuer)  // iss
                .issuedAt(Date.from(now))  // iat
                .expiration(Date.from(now.plusMillis(expiration)))  // exp
                .signWith(privateKey, Jwts.SIG.RS256)  // RS256 подпись
                .compact();
    }

    // Проверка токена
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Invalid token: " + e.getMessage());
        }
    }

    // Получить claims из токена без проверки (если нужно)
    public Map<String, Object> getClaims(String token) {
        return validateToken(token);
    }
}
