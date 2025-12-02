package com.lifepill.user_auth.repository;

import com.lifepill.user_auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations.
 * Provides data access methods for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by email address.
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with the given email.
     *
     * @param email the email address to check
     * @return true if a user exists with this email
     */
    boolean existsByEmail(String email);

    /**
     * Find a user by email verification token.
     *
     * @param token the verification token
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Find a user by password reset token.
     *
     * @param token the password reset token
     * @return an Optional containing the user if found
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Find a user by password reset token and check if not expired.
     *
     * @param token the password reset token
     * @param currentTime the current time for expiry comparison
     * @return an Optional containing the user if found and token not expired
     */
    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetTokenExpiresAt > :currentTime")
    Optional<User> findByPasswordResetTokenAndNotExpired(
            @Param("token") String token,
            @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * Find a user by email verification token and check if not expired.
     *
     * @param token the verification token
     * @param currentTime the current time for expiry comparison
     * @return an Optional containing the user if found and token not expired
     */
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token AND u.emailVerificationTokenExpiresAt > :currentTime")
    Optional<User> findByEmailVerificationTokenAndNotExpired(
            @Param("token") String token,
            @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * Update email verified status for a user.
     *
     * @param userId the user ID
     * @param verified the verification status
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = :verified, u.emailVerificationToken = null, u.emailVerificationTokenExpiresAt = null WHERE u.id = :userId")
    void updateEmailVerified(@Param("userId") UUID userId, @Param("verified") boolean verified);

    /**
     * Clear password reset token for a user.
     *
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = null, u.passwordResetTokenExpiresAt = null WHERE u.id = :userId")
    void clearPasswordResetToken(@Param("userId") UUID userId);

    /**
     * Find a user by OAuth provider and provider ID.
     *
     * @param authProvider the authentication provider (e.g., "google")
     * @param providerId the provider-specific user ID
     * @return an Optional containing the user if found
     */
    Optional<User> findByAuthProviderAndProviderId(String authProvider, String providerId);

    /**
     * Check if a user exists with the given provider ID.
     *
     * @param authProvider the authentication provider
     * @param providerId the provider-specific user ID
     * @return true if a user exists with this provider info
     */
    boolean existsByAuthProviderAndProviderId(String authProvider, String providerId);
}
