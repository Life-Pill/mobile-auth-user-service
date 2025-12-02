package com.lifepill.user_auth.service.impl;

import com.lifepill.user_auth.entity.RefreshToken;
import com.lifepill.user_auth.entity.User;
import com.lifepill.user_auth.repository.RefreshTokenRepository;
import com.lifepill.user_auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Implementation of RefreshTokenService for refresh token management.
 * Handles token creation, validation, and revocation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user, String tokenValue) {
        // Hash the token using SHA-256 (JWT tokens are too long for BCrypt's 72-byte limit)
        String tokenHash = hashToken(tokenValue);

        // Calculate expiration
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(refreshTokenExpirationMs / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findValidToken(String tokenValue) {
        String tokenHash = hashToken(tokenValue);
        return refreshTokenRepository.findValidByTokenHash(tokenHash, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void revokeToken(String tokenValue) {
        String tokenHash = hashToken(tokenValue);
        refreshTokenRepository.revokeByTokenHash(tokenHash);
        log.info("Refresh token revoked");
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllByUserId(user.getId());
        log.info("All refresh tokens revoked for user: {}", user.getId());
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 */6 * * *") // Run every 6 hours
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());
        log.info("Cleaned up expired and revoked refresh tokens");
    }

    /**
     * Hash a token using SHA-256.
     * This is used instead of BCrypt because JWT tokens exceed BCrypt's 72-byte limit.
     *
     * @param token the token to hash
     * @return the SHA-256 hash encoded as Base64
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
