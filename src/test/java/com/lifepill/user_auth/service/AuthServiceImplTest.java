//package com.lifepill.user_auth.service;
//
//import com.lifepill.user_auth.dto.request.*;
//import com.lifepill.user_auth.dto.response.AuthResponse;
//import com.lifepill.user_auth.dto.response.GoogleUserInfo;
//import com.lifepill.user_auth.dto.response.TokenResponse;
//import com.lifepill.user_auth.entity.User;
//import com.lifepill.user_auth.exception.*;
//import com.lifepill.user_auth.mapper.UserMapper;
//import com.lifepill.user_auth.repository.UserRepository;
//import com.lifepill.user_auth.service.impl.AuthServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for AuthServiceImpl.
// */
//@ExtendWith(MockitoExtension.class)
//class AuthServiceImplTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private JwtService jwtService;
//
//    @Mock
//    private RefreshTokenService refreshTokenService;
//
//    @Mock
//    private EmailService emailService;
//
//    @Mock
//    private GoogleOAuthService googleOAuthService;
//
//    @Mock
//    private UserMapper userMapper;
//
//    @InjectMocks
//    private AuthServiceImpl authService;
//
//    private RegisterRequest registerRequest;
//    private LoginRequest loginRequest;
//    private User testUser;
//
//    @BeforeEach
//    void setUp() {
//        registerRequest = RegisterRequest.builder()
//                .email("test@example.com")
//                .password("Password123!")
//                .firstName("John")
//                .lastName("Doe")
//                .phoneNumber("+1234567890")
//                .build();
//
//        loginRequest = LoginRequest.builder()
//                .email("test@example.com")
//                .password("Password123!")
//                .build();
//
//        testUser = User.builder()
//                .id(UUID.randomUUID())
//                .email("test@example.com")
//                .passwordHash("hashedPassword")
//                .firstName("John")
//                .lastName("Doe")
//                .emailVerified(true)
//                .build();
//    }
//
//    @Nested
//    @DisplayName("Register Tests")
//    class RegisterTests {
//
//        @Test
//        @DisplayName("Should register user successfully")
//        void shouldRegisterUserSuccessfully() {
//            // Given
//            when(userRepository.existsByEmail(anyString())).thenReturn(false);
//            when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
//            when(userRepository.save(any(User.class))).thenReturn(testUser);
//            when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
//            when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
//
//            // When
//            AuthResponse response = authService.register(registerRequest);
//
//            // Then
//            assertNotNull(response);
//            assertEquals(testUser.getId().toString(), response.getUserId());
//            assertEquals("accessToken", response.getAccessToken());
//            assertEquals("refreshToken", response.getRefreshToken());
//            verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
//        }
//
//        @Test
//        @DisplayName("Should throw exception when email already exists")
//        void shouldThrowExceptionWhenEmailExists() {
//            // Given
//            when(userRepository.existsByEmail(anyString())).thenReturn(true);
//
//            // When & Then
//            assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerRequest));
//            verify(userRepository, never()).save(any(User.class));
//        }
//    }
//
//    @Nested
//    @DisplayName("Login Tests")
//    class LoginTests {
//
//        @Test
//        @DisplayName("Should login user successfully")
//        void shouldLoginUserSuccessfully() {
//            // Given
//            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
//            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
//            when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
//            when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
//
//            // When
//            AuthResponse response = authService.login(loginRequest);
//
//            // Then
//            assertNotNull(response);
//            assertEquals(testUser.getId().toString(), response.getUserId());
//            assertEquals("accessToken", response.getAccessToken());
//        }
//
//        @Test
//        @DisplayName("Should throw exception for invalid email")
//        void shouldThrowExceptionForInvalidEmail() {
//            // Given
//            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
//
//            // When & Then
//            assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
//        }
//
//        @Test
//        @DisplayName("Should throw exception for invalid password")
//        void shouldThrowExceptionForInvalidPassword() {
//            // Given
//            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
//            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
//
//            // When & Then
//            assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
//        }
//    }
//
//    @Nested
//    @DisplayName("Password Reset Tests")
//    class PasswordResetTests {
//
//        @Test
//        @DisplayName("Should send password reset email")
//        void shouldSendPasswordResetEmail() {
//            // Given
//            ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
//            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
//            when(userRepository.save(any(User.class))).thenReturn(testUser);
//
//            // When
//            authService.forgotPassword(request);
//
//            // Then
//            verify(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());
//        }
//
//        @Test
//        @DisplayName("Should throw exception when email not found")
//        void shouldThrowExceptionWhenEmailNotFound() {
//            // Given
//            ForgotPasswordRequest request = new ForgotPasswordRequest("unknown@example.com");
//            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
//
//            // When & Then
//            assertThrows(EmailNotFoundException.class, () -> authService.forgotPassword(request));
//        }
//
//        @Test
//        @DisplayName("Should reset password successfully")
//        void shouldResetPasswordSuccessfully() {
//            // Given
//            ResetPasswordRequest request = new ResetPasswordRequest("validToken", "NewPassword123!");
//            when(userRepository.findByPasswordResetTokenAndNotExpired(anyString(), any(LocalDateTime.class)))
//                    .thenReturn(Optional.of(testUser));
//            when(passwordEncoder.encode(anyString())).thenReturn("newHashedPassword");
//            when(userRepository.save(any(User.class))).thenReturn(testUser);
//
//            // When
//            authService.resetPassword(request);
//
//            // Then
//            verify(refreshTokenService).revokeAllUserTokens(any(User.class));
//            verify(userRepository).save(any(User.class));
//        }
//
//        @Test
//        @DisplayName("Should throw exception for invalid reset token")
//        void shouldThrowExceptionForInvalidResetToken() {
//            // Given
//            ResetPasswordRequest request = new ResetPasswordRequest("invalidToken", "NewPassword123!");
//            when(userRepository.findByPasswordResetTokenAndNotExpired(anyString(), any(LocalDateTime.class)))
//                    .thenReturn(Optional.empty());
//
//            // When & Then
//            assertThrows(InvalidTokenException.class, () -> authService.resetPassword(request));
//        }
//    }
//
//    @Nested
//    @DisplayName("Email Verification Tests")
//    class EmailVerificationTests {
//
//        @Test
//        @DisplayName("Should verify email successfully")
//        void shouldVerifyEmailSuccessfully() {
//            // Given
//            VerifyEmailRequest request = new VerifyEmailRequest("validToken");
//            testUser.setEmailVerified(false);
//            when(userRepository.findByEmailVerificationTokenAndNotExpired(anyString(), any(LocalDateTime.class)))
//                    .thenReturn(Optional.of(testUser));
//            when(userRepository.save(any(User.class))).thenReturn(testUser);
//
//            // When
//            authService.verifyEmail(request);
//
//            // Then
//            verify(emailService).sendWelcomeEmail(anyString(), anyString());
//        }
//
//        @Test
//        @DisplayName("Should throw exception for invalid verification token")
//        void shouldThrowExceptionForInvalidVerificationToken() {
//            // Given
//            VerifyEmailRequest request = new VerifyEmailRequest("invalidToken");
//            when(userRepository.findByEmailVerificationTokenAndNotExpired(anyString(), any(LocalDateTime.class)))
//                    .thenReturn(Optional.empty());
//
//            // When & Then
//            assertThrows(InvalidTokenException.class, () -> authService.verifyEmail(request));
//        }
//    }
//
//    @Nested
//    @DisplayName("Token Refresh Tests")
//    class TokenRefreshTests {
//
//        @Test
//        @DisplayName("Should throw exception for invalid refresh token format")
//        void shouldThrowExceptionForInvalidRefreshTokenFormat() {
//            // Given
//            RefreshTokenRequest request = new RefreshTokenRequest("invalidToken");
//            when(jwtService.validateToken(anyString())).thenReturn(false);
//
//            // When & Then
//            assertThrows(InvalidTokenException.class, () -> authService.refreshToken(request));
//        }
//    }
//
//    @Nested
//    @DisplayName("Logout Tests")
//    class LogoutTests {
//
//        @Test
//        @DisplayName("Should logout successfully")
//        void shouldLogoutSuccessfully() {
//            // Given
//            LogoutRequest request = new LogoutRequest("refreshToken");
//
//            // When
//            authService.logout(request);
//
//            // Then
//            verify(refreshTokenService).revokeToken(anyString());
//        }
//    }
//
//    @Nested
//    @DisplayName("Google Sign-In Tests")
//    class GoogleSignInTests {
//
//        @Test
//        @DisplayName("Should create new user on Google Sign-In")
//        void shouldCreateNewUserOnGoogleSignIn() {
//            // Given
//            GoogleSignInRequest request = new GoogleSignInRequest("validGoogleToken", "device123");
//            GoogleUserInfo googleUserInfo = GoogleUserInfo.builder()
//                    .googleId("google123")
//                    .email("google@example.com")
//                    .firstName("Google")
//                    .lastName("User")
//                    .pictureUrl("https://example.com/picture.jpg")
//                    .emailVerified(true)
//                    .build();
//
//            User newUser = User.builder()
//                    .id(UUID.randomUUID())
//                    .email("google@example.com")
//                    .firstName("Google")
//                    .lastName("User")
//                    .authProvider("google")
//                    .providerId("google123")
//                    .emailVerified(true)
//                    .build();
//
//            when(googleOAuthService.verifyIdToken(anyString())).thenReturn(googleUserInfo);
//            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
//            when(passwordEncoder.encode(anyString())).thenReturn("randomHash");
//            when(userRepository.save(any(User.class))).thenReturn(newUser);
//            when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
//            when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
//
//            // When
//            AuthResponse response = authService.googleSignIn(request);
//
//            // Then
//            assertNotNull(response);
//            assertEquals(newUser.getId().toString(), response.getUserId());
//            verify(emailService).sendWelcomeEmail(anyString(), anyString());
//        }
//
//        @Test
//        @DisplayName("Should login existing user on Google Sign-In")
//        void shouldLoginExistingUserOnGoogleSignIn() {
//            // Given
//            GoogleSignInRequest request = new GoogleSignInRequest("validGoogleToken", null);
//            GoogleUserInfo googleUserInfo = GoogleUserInfo.builder()
//                    .googleId("google123")
//                    .email("existing@example.com")
//                    .firstName("Existing")
//                    .lastName("User")
//                    .emailVerified(true)
//                    .build();
//
//            testUser.setAuthProvider("google");
//            testUser.setProviderId("google123");
//
//            when(googleOAuthService.verifyIdToken(anyString())).thenReturn(googleUserInfo);
//            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
//            when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
//            when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
//
//            // When
//            AuthResponse response = authService.googleSignIn(request);
//
//            // Then
//            assertNotNull(response);
//            assertEquals(testUser.getId().toString(), response.getUserId());
//            verify(emailService, never()).sendWelcomeEmail(anyString(), anyString());
//        }
//
//        @Test
//        @DisplayName("Should link local account with Google")
//        void shouldLinkLocalAccountWithGoogle() {
//            // Given
//            GoogleSignInRequest request = new GoogleSignInRequest("validGoogleToken", null);
//            GoogleUserInfo googleUserInfo = GoogleUserInfo.builder()
//                    .googleId("google123")
//                    .email("test@example.com")
//                    .firstName("John")
//                    .lastName("Doe")
//                    .pictureUrl("https://example.com/picture.jpg")
//                    .emailVerified(true)
//                    .build();
//
//            testUser.setAuthProvider("local");
//
//            when(googleOAuthService.verifyIdToken(anyString())).thenReturn(googleUserInfo);
//            when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
//            when(userRepository.save(any(User.class))).thenReturn(testUser);
//            when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
//            when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
//
//            // When
//            AuthResponse response = authService.googleSignIn(request);
//
//            // Then
//            assertNotNull(response);
//            verify(userRepository).save(any(User.class));
//        }
//
//        @Test
//        @DisplayName("Should throw exception for invalid Google token")
//        void shouldThrowExceptionForInvalidGoogleToken() {
//            // Given
//            GoogleSignInRequest request = new GoogleSignInRequest("invalidGoogleToken", null);
//            when(googleOAuthService.verifyIdToken(anyString()))
//                    .thenThrow(new InvalidTokenException("Invalid Google ID token"));
//
//            // When & Then
//            assertThrows(InvalidTokenException.class, () -> authService.googleSignIn(request));
//        }
//    }
//}
