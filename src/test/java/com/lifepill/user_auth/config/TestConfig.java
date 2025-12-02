package com.lifepill.user_auth.config;

import com.lifepill.user_auth.service.EmailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration that provides mock beans for testing.
 */
@TestConfiguration
public class TestConfig {

    /**
     * Mock email service for testing.
     * Prevents actual emails from being sent during tests.
     */
    @Bean
    @Primary
    public EmailService testEmailService() {
        return new EmailService() {
            @Override
            public void sendVerificationEmail(String email, String token, String firstName) {
                // No-op for testing
            }

            @Override
            public void sendPasswordResetEmail(String email, String token, String firstName) {
                // No-op for testing
            }

            @Override
            public void sendWelcomeEmail(String email, String firstName) {
                // No-op for testing
            }
        };
    }
}
