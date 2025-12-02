package com.lifepill.user_auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User entity representing the users table in the database.
 * This is the core entity for user authentication and profile management.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expires_at")
    private LocalDateTime emailVerificationTokenExpiresAt;

    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;

    @Column(name = "password_reset_token_expires_at")
    private LocalDateTime passwordResetTokenExpiresAt;

    // OAuth fields
    @Column(name = "auth_provider", length = 50)
    private String authProvider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    /**
     * Get the primary address of the user.
     *
     * @return the primary UserAddress or null if none exists
     */
    public UserAddress getPrimaryAddress() {
        return addresses.stream()
                .filter(UserAddress::getIsPrimary)
                .findFirst()
                .orElse(null);
    }

    /**
     * Add an address to the user's address list.
     *
     * @param address the address to add
     */
    public void addAddress(UserAddress address) {
        addresses.add(address);
        address.setUser(this);
    }

    /**
     * Remove an address from the user's address list.
     *
     * @param address the address to remove
     */
    public void removeAddress(UserAddress address) {
        addresses.remove(address);
        address.setUser(null);
    }

    /**
     * Get the user's full name.
     *
     * @return concatenated first and last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
