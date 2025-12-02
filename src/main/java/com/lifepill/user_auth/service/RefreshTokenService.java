package com.lifepill.user_auth.service;

import com.lifepill.user_auth.entity.RefreshToken;
import com.lifepill.user_auth.entity.User;

import java.util.Optional;

/**
 * Service interface for refresh token management.
 * Defines the contract for refresh token operations.
 */
public interface RefreshTokenService {

    /**
     * Create a new refresh token for a user.
     *
     * @param user the user entity
     * @param tokenValue the raw token value
     * @return the created refresh token entity
     */
    RefreshToken createRefreshToken(User user, String tokenValue);

    /**
     * Find a valid refresh token by its raw value.
     *
     * @param tokenValue the raw token value
     * @return an Optional containing the refresh token if valid
     */
    Optional<RefreshToken> findValidToken(String tokenValue);

    /**
     * Revoke a refresh token.
     *
     * @param tokenValue the raw token value
     */
    void revokeToken(String tokenValue);

    /**
     * Revoke all refresh tokens for a user.
     *
     * @param user the user entity
     */
    void revokeAllUserTokens(User user);

    /**
     * Clean up expired and revoked tokens.
     */
    void cleanupExpiredTokens();
}
