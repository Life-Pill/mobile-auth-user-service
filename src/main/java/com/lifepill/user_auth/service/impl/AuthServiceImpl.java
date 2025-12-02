package com.lifepill.user_auth.service.impl;

import com.lifepill.user_auth.dto.request.*;
import com.lifepill.user_auth.dto.response.*;
import com.lifepill.user_auth.entity.RefreshToken;
import com.lifepill.user_auth.entity.User;
import com.lifepill.user_auth.entity.UserAddress;
import com.lifepill.user_auth.exception.*;
import com.lifepill.user_auth.mapper.UserMapper;
import com.lifepill.user_auth.repository.UserRepository;
import com.lifepill.user_auth.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of AuthService for authentication operations.
 * Handles user registration, login, password reset, and token management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int VERIFICATION_TOKEN_EXPIRY_HOURS = 24;
    private static final int PASSWORD_RESET_TOKEN_EXPIRY_HOURS = 1;
    private static final String AUTH_PROVIDER_LOCAL = "local";
    private static final String AUTH_PROVIDER_GOOGLE = "google";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final GoogleOAuthService googleOAuthService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // Create user entity
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth() != null ? LocalDate.parse(request.getDateOfBirth()) : null)
                .emailVerified(false)
                .authProvider(AUTH_PROVIDER_LOCAL)
                .emailVerificationToken(generateSecureToken())
                .emailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_EXPIRY_HOURS))
                .build();

        // Add address if provided
        if (request.getAddress() != null) {
            UserAddress address = userMapper.toUserAddress(request.getAddress());
            user.addAddress(address);
        }

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Send verification email
        emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getEmailVerificationToken(),
                savedUser.getFirstName()
        );

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        // Save refresh token
        refreshTokenService.createRefreshToken(savedUser, refreshToken);

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password attempt for email: {}", request.getEmail());
            throw new InvalidCredentialsException();
        }

        // Check if email is verified (optional - can be configured)
        // Uncomment the following lines to require email verification before login
        // if (!user.getEmailVerified()) {
        //     throw new EmailNotVerifiedException();
        // }

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save refresh token
        refreshTokenService.createRefreshToken(user, refreshToken);

        log.info("User logged in successfully: {}", user.getId());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse googleSignIn(GoogleSignInRequest request) {
        log.info("Processing Google Sign-In");

        // Verify Google ID token
        GoogleUserInfo googleUserInfo = googleOAuthService.verifyIdToken(request.getIdToken());
        String email = googleUserInfo.getEmail().toLowerCase();

        // Check if user exists
        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        User user;
        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            
            // Update user with Google info if needed
            if (user.getAuthProvider() == null || AUTH_PROVIDER_LOCAL.equals(user.getAuthProvider())) {
                // Link existing local account with Google
                user.setProviderId(googleUserInfo.getGoogleId());
                user.setAuthProvider(AUTH_PROVIDER_GOOGLE);
                user.setEmailVerified(true); // Google verified the email
                if (googleUserInfo.getPictureUrl() != null) {
                    user.setProfilePictureUrl(googleUserInfo.getPictureUrl());
                }
                userRepository.save(user);
                log.info("Linked existing local account with Google for user: {}", user.getId());
            } else if (AUTH_PROVIDER_GOOGLE.equals(user.getAuthProvider())) {
                // Update profile picture if changed
                if (googleUserInfo.getPictureUrl() != null && 
                        !googleUserInfo.getPictureUrl().equals(user.getProfilePictureUrl())) {
                    user.setProfilePictureUrl(googleUserInfo.getPictureUrl());
                    userRepository.save(user);
                }
            }
        } else {
            // Create new user from Google data
            user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(generateSecureToken())) // Random secure password
                    .firstName(googleUserInfo.getFirstName())
                    .lastName(googleUserInfo.getLastName())
                    .emailVerified(true) // Google verified
                    .authProvider(AUTH_PROVIDER_GOOGLE)
                    .providerId(googleUserInfo.getGoogleId())
                    .profilePictureUrl(googleUserInfo.getPictureUrl())
                    .build();

            user = userRepository.save(user);
            log.info("Created new user from Google Sign-In: {}", user.getId());

            // Send welcome email
            emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
        }

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Save refresh token
        refreshTokenService.createRefreshToken(user, refreshToken);

        log.info("Google Sign-In successful for user: {}", user.getId());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Processing forgot password for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(EmailNotFoundException::new);

        // Generate password reset token
        String resetToken = generateSecureToken();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusHours(PASSWORD_RESET_TOKEN_EXPIRY_HOURS));

        userRepository.save(user);

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken, user.getFirstName());

        log.info("Password reset email sent to: {}", request.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Processing password reset with token");

        User user = userRepository.findByPasswordResetTokenAndNotExpired(
                        request.getToken(),
                        LocalDateTime.now()
                )
                .orElseThrow(InvalidTokenException::expiredResetToken);

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);

        userRepository.save(user);

        // Revoke all existing refresh tokens for security
        refreshTokenService.revokeAllUserTokens(user);

        log.info("Password reset successfully for user: {}", user.getId());
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        log.info("Processing email verification with token");

        User user = userRepository.findByEmailVerificationTokenAndNotExpired(
                        request.getToken(),
                        LocalDateTime.now()
                )
                .orElseThrow(InvalidTokenException::expiredVerificationToken);

        // Update email verified status
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiresAt(null);

        userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        log.info("Email verified successfully for user: {}", user.getId());
    }

    @Override
    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        log.info("Processing resend verification for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(EmailNotFoundException::new);

        if (user.getEmailVerified()) {
            log.warn("Email already verified for: {}", request.getEmail());
            return; // Silently return to prevent email enumeration
        }

        // Generate new verification token
        String verificationToken = generateSecureToken();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(VERIFICATION_TOKEN_EXPIRY_HOURS));

        userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), verificationToken, user.getFirstName());

        log.info("Verification email resent to: {}", request.getEmail());
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        log.info("Processing token refresh");

        // Validate the refresh token format
        if (!jwtService.validateToken(request.getRefreshToken()) ||
                !jwtService.isRefreshToken(request.getRefreshToken())) {
            throw InvalidTokenException.invalidRefreshToken();
        }

        // Find valid refresh token in database
        RefreshToken storedToken = refreshTokenService.findValidToken(request.getRefreshToken())
                .orElseThrow(InvalidTokenException::invalidRefreshToken);

        User user = storedToken.getUser();

        // Revoke old refresh token
        refreshTokenService.revokeToken(request.getRefreshToken());

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // Save new refresh token
        refreshTokenService.createRefreshToken(user, newRefreshToken);

        log.info("Token refreshed successfully for user: {}", user.getId());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        log.info("Processing logout");

        // Revoke the refresh token
        refreshTokenService.revokeToken(request.getRefreshToken());

        log.info("User logged out successfully");
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        UserAddress primaryAddress = user.getPrimaryAddress();
        AddressResponse addressResponse = primaryAddress != null
                ? userMapper.toAddressResponse(primaryAddress)
                : null;

        ProfileData profileData = ProfileData.builder()
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null)
                .address(addressResponse)
                .build();

        return AuthResponse.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.getEmailVerified())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .profile(profileData)
                .build();
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "");
    }
}
