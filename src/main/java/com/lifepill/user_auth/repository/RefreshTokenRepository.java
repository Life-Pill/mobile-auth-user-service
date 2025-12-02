package com.lifepill.user_auth.repository;

import com.lifepill.user_auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for RefreshToken entity operations.
 * Provides data access methods for refresh token management.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find a refresh token by its hash.
     *
     * @param tokenHash the token hash
     * @return an Optional containing the refresh token if found
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Find a valid (not revoked, not expired) refresh token by its hash.
     *
     * @param tokenHash the token hash
     * @param currentTime the current time for expiry comparison
     * @return an Optional containing the refresh token if found and valid
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash AND rt.revoked = false AND rt.expiresAt > :currentTime")
    Optional<RefreshToken> findValidByTokenHash(
            @Param("tokenHash") String tokenHash,
            @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * Revoke all refresh tokens for a user.
     *
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllByUserId(@Param("userId") UUID userId);

    /**
     * Revoke a specific refresh token by its hash.
     *
     * @param tokenHash the token hash
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.tokenHash = :tokenHash")
    void revokeByTokenHash(@Param("tokenHash") String tokenHash);

    /**
     * Delete all expired or revoked tokens.
     * Used for periodic cleanup.
     *
     * @param currentTime the current time for expiry comparison
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true OR rt.expiresAt < :currentTime")
    void deleteExpiredAndRevoked(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Delete all refresh tokens for a user.
     *
     * @param userId the user ID
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    /**
     * Count active refresh tokens for a user.
     *
     * @param userId the user ID
     * @param currentTime the current time for expiry comparison
     * @return the count of active tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false AND rt.expiresAt > :currentTime")
    long countActiveByUserId(@Param("userId") UUID userId, @Param("currentTime") LocalDateTime currentTime);
}
