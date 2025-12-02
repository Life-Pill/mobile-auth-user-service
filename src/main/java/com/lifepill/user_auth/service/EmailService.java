package com.lifepill.user_auth.service;

/**
 * Service interface for email operations.
 * Defines the contract for sending various types of emails.
 */
public interface EmailService {

    /**
     * Send email verification link.
     *
     * @param email the recipient email
     * @param token the verification token
     * @param firstName the user's first name
     */
    void sendVerificationEmail(String email, String token, String firstName);

    /**
     * Send password reset link.
     *
     * @param email the recipient email
     * @param token the reset token
     * @param firstName the user's first name
     */
    void sendPasswordResetEmail(String email, String token, String firstName);

    /**
     * Send welcome email after successful registration.
     *
     * @param email the recipient email
     * @param firstName the user's first name
     */
    void sendWelcomeEmail(String email, String firstName);
}
