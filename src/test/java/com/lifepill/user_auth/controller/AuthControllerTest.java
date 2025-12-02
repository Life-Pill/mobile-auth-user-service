package com.lifepill.user_auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepill.user_auth.config.RateLimiterConfig;
import com.lifepill.user_auth.dto.request.LoginRequest;
import com.lifepill.user_auth.dto.request.RegisterRequest;
import com.lifepill.user_auth.dto.response.AuthResponse;
import com.lifepill.user_auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AuthController.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private RateLimiterConfig rateLimiterConfig;

    @Test
    @DisplayName("Should register user successfully")
    @WithMockUser
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        AuthResponse response = AuthResponse.builder()
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .emailVerified(false)
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(rateLimiterConfig.allowRegisterAttempt(any())).thenReturn(true);
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("user-123"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Should return validation error for invalid email")
    @WithMockUser
    void shouldReturnValidationErrorForInvalidEmail() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(rateLimiterConfig.allowRegisterAttempt(any())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should login user successfully")
    @WithMockUser
    void shouldLoginUserSuccessfully() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();

        AuthResponse response = AuthResponse.builder()
                .userId("user-123")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .emailVerified(true)
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(rateLimiterConfig.allowLoginAttempt(any())).thenReturn(true);
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("Should return rate limit error")
    @WithMockUser
    void shouldReturnRateLimitError() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();

        when(rateLimiterConfig.allowLoginAttempt(any())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }
}
