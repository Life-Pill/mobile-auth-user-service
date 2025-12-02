package com.lifepill.user_auth.service;

import com.lifepill.user_auth.entity.User;

import java.util.UUID;

/**
 * Service interface for JWT token operations.
 * Defines the contract for token generation and validation.
 */
public interface JwtService {

    /**
     * Generate access token for a user.
     *
     * @param user the user entity
     * @return the generated access token
     */
    String generateAccessToken(User user);

    /**
     * Generate refresh token for a user.
     *
     * @param user the user entity
     * @return the generated refresh token
     */
    String generateRefreshToken(User user);

    /**
     * Extract user ID from token.
     *
     * @param token the JWT token
     * @return the user ID
     */
    UUID extractUserId(String token);

    /**
     * Extract email from token.
     *
     * @param token the JWT token
     * @return the email
     */
    String extractEmail(String token);

    /**
     * Validate a token.
     *
     * @param token the JWT token
     * @return true if valid, false otherwise
     */
    boolean validateToken(String token);

    /**
     * Check if token is an access token.
     *
     * @param token the JWT token
     * @return true if access token
     */
    boolean isAccessToken(String token);

    /**
     * Check if token is a refresh token.
     *
     * @param token the JWT token
     * @return true if refresh token
     */
    boolean isRefreshToken(String token);

    /**
     * Get the refresh token expiration time in milliseconds.
     *
     * @return expiration time in milliseconds
     */
    long getRefreshTokenExpirationMs();
}
