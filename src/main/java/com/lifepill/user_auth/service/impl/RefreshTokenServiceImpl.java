package com.lifepill.user_auth.service.impl;

import com.lifepill.user_auth.entity.RefreshToken;
import com.lifepill.user_auth.entity.User;
import com.lifepill.user_auth.repository.RefreshTokenRepository;
import com.lifepill.user_auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user, String tokenValue) {
        // Hash the token for storage
        String tokenHash = passwordEncoder.encode(tokenValue);

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
        // We need to find all non-revoked, non-expired tokens and check the hash
        // This is a simplified approach - in production, you might use a different strategy
        return refreshTokenRepository.findAll().stream()
                .filter(token -> !token.getRevoked())
                .filter(token -> !token.isExpired())
                .filter(token -> passwordEncoder.matches(tokenValue, token.getTokenHash()))
                .findFirst();
    }

    @Override
    @Transactional
    public void revokeToken(String tokenValue) {
        findValidToken(tokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("Refresh token revoked for user: {}", token.getUser().getId());
        });
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
}
