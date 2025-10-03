package com.example.controller;

import com.example.dto.TokenResponse;
import com.example.dto.UserDto;
import com.example.dto.UserUpdateDto;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.exception.ValidationException;
import com.example.mapper.UserMapper;
import com.example.repository.UserRepository;
import com.example.service.AuthService;
import com.example.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil; // Мокаем JwtUtil для JwtAuthenticationFilter

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private UserDto userDto;
    private UserUpdateDto userUpdateDto;
    private TokenResponse tokenResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPasswordHash("hashedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setMiddleName("");
        user.setEmail("test@example.com");
        user.setRole(Role.ROLE_USER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setMiddleName("");
        userDto.setRole(Role.ROLE_USER.name());
        userDto.setActive(true);

        userUpdateDto = new UserUpdateDto();
        userUpdateDto.setUsername("testuser");
        userUpdateDto.setEmail("test@example.com");
        userUpdateDto.setFirstName("Updated");
        userUpdateDto.setLastName("User");
        userUpdateDto.setMiddleName("Middle");
        userUpdateDto.setRole(Role.ROLE_USER.name());
        userUpdateDto.setActive(true);

        tokenResponse = new TokenResponse("access-token", "refresh-token", "Bearer", 3600000L);
    }

    // logout tests
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void logout_shouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out"));

        verify(authService).logout("refresh-token");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void logout_shouldReturnValidationException() throws Exception {
        doThrow(new ValidationException("Invalid refresh token")).when(authService).logout("invalid-token");

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"invalid-token\"}"))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));

        verify(authService).logout("invalid-token");
    }

    // updateMe tests
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateMe_shouldUpdateUser() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);
        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(put("/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userMapper).updateFromDto(userUpdateDto, user);
        verify(userRepository).save(user);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateMe_shouldThrowNotFound_whenUserNotFound() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(null);

        mockMvc.perform(put("/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDto)))
                .andExpect(status().isNotFound()) // 404
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userMapper, never()).updateFromDto(any(), any());
        verify(userRepository, never()).save(any());
    }

    // getMe tests
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getMe_shouldReturnUser() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userMapper).toDto(user);
    }

    @Test
    void getMe_shouldThrowRuntimeException_whenPrincipalNull() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized()) // 401
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verify(userRepository, never()).findByUsername(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getMe_shouldThrowNotFound_whenUserNotFound() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(null);

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isNotFound()) // 404
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userMapper, never()).toDto(any());
    }

//    // test endpoint
//    @Test
//    void test_shouldReturnWorkingMessage() throws Exception {
//        mockMvc.perform(get("/test"))
//                .andExpect(status().isOk())
//                .andExpect(content().string("Auth-Gateway is working!"));
//    }

    // register tests
    @Test
    void register_shouldReturnSuccess() throws Exception {
        when(authService.register(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registered successfully"));

        verify(authService).register(any(User.class));
    }

    @Test
    void register_shouldThrowValidationException() throws Exception {
        doThrow(new ValidationException("Username already exists")).when(authService).register(any(User.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.message").value("Username already exists"));

        verify(authService).register(any(User.class));
    }

    // login tests
    @Test
    void login_shouldReturnTokenResponse() throws Exception {
        when(authService.login("testuser", "password")).thenReturn(tokenResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testuser\", \"password\": \"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600000));

        verify(authService).login("testuser", "password");
    }

    @Test
    void login_shouldThrowValidationException() throws Exception {
        doThrow(new ValidationException("Invalid credentials")).when(authService).login("testuser", "wrong-password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testuser\", \"password\": \"wrong-password\"}"))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.message").value("Invalid credentials"));

        verify(authService).login("testuser", "wrong-password");
    }

    @Test
    void login_shouldThrowRuntimeException() throws Exception {
        doThrow(new RuntimeException("User not active")).when(authService).login("testuser", "password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testuser\", \"password\": \"password\"}"))
                .andExpect(status().isInternalServerError()) // 500
                .andExpect(jsonPath("$.message").value("User not active"));

        verify(authService).login("testuser", "password");
    }

    // refresh tests
    @Test
    void refresh_shouldReturnTokenResponse() throws Exception {
        when(authService.refresh("refresh-token")).thenReturn(tokenResponse);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600000));

        verify(authService).refresh("refresh-token");
    }

    @Test
    void refresh_shouldThrowValidationException() throws Exception {
        doThrow(new ValidationException("Invalid refresh token")).when(authService).refresh("invalid-token");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"invalid-token\"}"))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));

        verify(authService).refresh("invalid-token");
    }
}
