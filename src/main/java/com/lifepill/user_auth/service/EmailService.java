package com.lifepill.user_auth.service;

import com.lifepill.user_auth.exception.EmailSendException;

/**
 * Service interface for email operations.
 * Defines the contract for sending various types of emails.
 */
public interface EmailService {

    /**
     * Send email verification link (async - fire and forget).
     *
     * @param email the recipient email
     * @param token the verification token
     * @param firstName the user's first name
     */
    void sendVerificationEmail(String email, String token, String firstName);

    /**
     * Send password reset link synchronously with proper error handling.
     * This method throws an exception if email sending fails.
     *
     * @param email the recipient email
     * @param token the reset token
     * @param firstName the user's first name
     * @throws EmailSendException if email sending fails
     */
    void sendPasswordResetEmailSync(String email, String token, String firstName) throws EmailSendException;

    /**
     * Send password reset link (async - fire and forget for background operations).
     *
     * @param email the recipient email
     * @param token the reset token
     * @param firstName the user's first name
     */
    void sendPasswordResetEmail(String email, String token, String firstName);

    /**
     * Send welcome email after successful registration (async).
     *
     * @param email the recipient email
     * @param firstName the user's first name
     */
    void sendWelcomeEmail(String email, String firstName);

    /**
     * Test email configuration by sending a test email.
     *
     * @param email the recipient email
     * @throws EmailSendException if email sending fails
     */
    void testEmailConfiguration(String email) throws EmailSendException;
}
