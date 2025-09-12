package com.example.controller;

import com.example.dto.TokenResponse;
import com.example.dto.UserDto;
import com.example.dto.UserUpdateDto;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.repository.UserRepository;
import com.example.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;

    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> body) {
        authService.logout(body.get("refreshToken"));
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @PutMapping("/auth/me")
    public ResponseEntity<UserDto> updateMe(@Valid @RequestBody UserUpdateDto dto,
                                            Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        userMapper.updateFromDto(dto, user);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<UserDto> getMe(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }
        User user = userRepository.findByUsername(principal.getName());
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @GetMapping("/test")
    public String test() {
        return "Auth-Gateway is working!";
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        authService.register(user);
        return ResponseEntity.ok(Map.of("message", "Registered successfully"));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<TokenResponse> login(@RequestBody Map<String, String> credentials) {
        return ResponseEntity.ok(authService.login(credentials.get("username"), credentials.get("password")));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.refresh(body.get("refreshToken")));
    }
}
