package com.lifepill.user_auth.service;

import com.lifepill.user_auth.dto.request.*;
import com.lifepill.user_auth.dto.response.AuthResponse;
import com.lifepill.user_auth.dto.response.TokenResponse;

/**
 * Service interface for authentication operations.
 * Defines the contract for authentication-related business logic.
 */
public interface AuthService {

    /**
     * Register a new user.
     *
     * @param request the registration request
     * @return the authentication response with tokens
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate a user and generate tokens.
     *
     * @param request the login request
     * @return the authentication response with tokens
     */
    AuthResponse login(LoginRequest request);

    /**
     * Authenticate user via Google Sign-In.
     *
     * @param request the Google sign-in request containing ID token
     * @return the authentication response with tokens
     */
    AuthResponse googleSignIn(GoogleSignInRequest request);

    /**
     * Send password reset email.
     *
     * @param request the forgot password request
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Reset user password using token.
     *
     * @param request the reset password request
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * Verify user email using token.
     *
     * @param request the verify email request
     */
    void verifyEmail(VerifyEmailRequest request);

    /**
     * Resend email verification link.
     *
     * @param request the resend verification request
     */
    void resendVerification(ResendVerificationRequest request);

    /**
     * Refresh access token using refresh token.
     *
     * @param request the refresh token request
     * @return the new token response
     */
    TokenResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logout user and invalidate refresh token.
     *
     * @param request the logout request
     */
    void logout(LogoutRequest request);
}
