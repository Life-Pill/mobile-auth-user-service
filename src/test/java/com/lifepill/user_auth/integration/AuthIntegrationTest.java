package com.lifepill.user_auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifepill.user_auth.config.TestConfig;
import com.lifepill.user_auth.dto.request.LoginRequest;
import com.lifepill.user_auth.dto.request.RegisterRequest;
import com.lifepill.user_auth.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for authentication flow.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private static String accessToken;
    private static String refreshToken;

    @BeforeEach
    void setUp() {
        // Clean up before each test
    }

    @AfterEach
    void tearDown() {
        // Optional cleanup
    }

    @Test
    @Order(1)
    @DisplayName("Should register a new user")
    void shouldRegisterNewUser() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("integration@test.com")
                .password("Password123!")
                .firstName("Integration")
                .lastName("Test")
                .phoneNumber("+1234567890")
                .build();

        // When & Then
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.userId").isNotEmpty())
                .andExpect(jsonPath("$.data.email").value("integration@test.com"))
                .andExpect(jsonPath("$.data.firstName").value("Integration"))
                .andExpect(jsonPath("$.data.lastName").value("Test"))
                .andExpect(jsonPath("$.data.emailVerified").value(false))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        // Store tokens for later tests
        String responseBody = result.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(responseBody).path("data").path("accessToken").asText();
        refreshToken = objectMapper.readTree(responseBody).path("data").path("refreshToken").asText();
    }

    @Test
    @Order(2)
    @DisplayName("Should fail to register with existing email")
    void shouldFailToRegisterWithExistingEmail() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("integration@test.com")
                .password("Password123!")
                .firstName("Duplicate")
                .lastName("User")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    @Order(3)
    @DisplayName("Should login with valid credentials")
    void shouldLoginWithValidCredentials() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("integration@test.com")
                .password("Password123!")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("Should fail login with invalid password")
    void shouldFailLoginWithInvalidPassword() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("integration@test.com")
                .password("WrongPassword123!")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @Order(5)
    @DisplayName("Should fail login with non-existent email")
    void shouldFailLoginWithNonExistentEmail() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@test.com")
                .password("Password123!")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @Order(6)
    @DisplayName("Should validate password requirements")
    void shouldValidatePasswordRequirements() throws Exception {
        // Given - weak password
        RegisterRequest request = RegisterRequest.builder()
                .email("weakpass@test.com")
                .password("weak")
                .firstName("Weak")
                .lastName("Password")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @Order(7)
    @DisplayName("Should validate email format")
    void shouldValidateEmailFormat() throws Exception {
        // Given - invalid email
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .password("Password123!")
                .firstName("Invalid")
                .lastName("Email")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
