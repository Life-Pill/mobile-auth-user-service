package com.lifepill.user_auth.service.impl;

import com.lifepill.user_auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Implementation of EmailService for sending emails.
 * Handles verification, password reset, and welcome emails.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.verification.url}")
    private String verificationUrl;

    @Value("${app.password.reset.url}")
    private String passwordResetUrl;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Async
    public void sendVerificationEmail(String email, String token, String firstName) {
        try {
            String subject = "Verify your LifePill account";
            String verifyLink = verificationUrl + "?token=" + token;
            
            String body = String.format("""
                    Hello %s,
                    
                    Thank you for registering with LifePill!
                    
                    Please click the link below to verify your email address:
                    %s
                    
                    This link will expire in 24 hours.
                    
                    If you didn't create an account with LifePill, please ignore this email.
                    
                    Best regards,
                    The LifePill Team
                    """, firstName, verifyLink);

            sendEmail(email, subject, body);
            log.info("Verification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", email, e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String email, String token, String firstName) {
        try {
            String subject = "Reset your LifePill password";
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            
            String body = String.format("""
                    Hello %s,
                    
                    We received a request to reset your LifePill password.
                    
                    Please click the link below to reset your password:
                    %s
                    
                    This link will expire in 1 hour.
                    
                    If you didn't request a password reset, please ignore this email.
                    Your password will remain unchanged.
                    
                    Best regards,
                    The LifePill Team
                    """, firstName, resetLink);

            sendEmail(email, subject, body);
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(String email, String firstName) {
        try {
            String subject = "Welcome to LifePill!";
            
            String body = String.format("""
                    Hello %s,
                    
                    Welcome to LifePill! Your account has been verified successfully.
                    
                    You can now access all features of the LifePill application.
                    
                    If you have any questions, feel free to contact our support team.
                    
                    Best regards,
                    The LifePill Team
                    """, firstName);

            sendEmail(email, subject, body);
            log.info("Welcome email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", email, e);
        }
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
