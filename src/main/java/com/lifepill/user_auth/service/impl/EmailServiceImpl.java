package com.lifepill.user_auth.service.impl;

import com.lifepill.user_auth.exception.EmailSendException;
import com.lifepill.user_auth.service.EmailService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Implementation of EmailService for sending professional HTML emails.
 * Uses external templates and environment-based configuration.
 * Handles verification, password reset, and welcome emails with LifePill branding.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    // Mail Configuration
    @Value("${spring.mail.username}")
    private String fromEmail;

    // URL Configuration
    @Value("${app.email.verification.url}")
    private String verificationUrl;

    @Value("${app.password.reset.url}")
    private String passwordResetUrl;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // Expiry Configuration
    @Value("${app.email.verification.expiry-hours:24}")
    private int verificationExpiryHours;

    @Value("${app.password.reset.expiry-hours:1}")
    private int passwordResetExpiryHours;

    // Branding Configuration
    @Value("${app.branding.logo-url}")
    private String logoUrl;

    @Value("${app.branding.support-email}")
    private String supportEmail;

    @Value("${app.branding.website-url}")
    private String websiteUrl;

    // Template cache
    private String verificationEmailTemplate;
    private String passwordResetEmailTemplate;
    private String welcomeEmailTemplate;

    @PostConstruct
    public void init() {
        try {
            verificationEmailTemplate = loadTemplate("templates/email/verification-email.html");
            passwordResetEmailTemplate = loadTemplate("templates/email/password-reset-email.html");
            welcomeEmailTemplate = loadTemplate("templates/email/welcome-email.html");
            log.info("Email templates loaded successfully");
        } catch (IOException e) {
            log.error("Failed to load email templates", e);
            throw new RuntimeException("Failed to load email templates", e);
        }
    }

    /**
     * Load HTML template from classpath resources.
     */
    private String loadTemplate(String templatePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    /**
     * Replace placeholders in template with actual values.
     */
    private String processTemplate(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    @Override
    @Async
    public void sendVerificationEmail(String email, String token, String firstName) {
        try {
            String subject = "✉️ Verify Your LifePill Account";
            String verifyLink = verificationUrl + "?token=" + token;

            Map<String, String> variables = Map.of(
                    "firstName", firstName,
                    "verifyLink", verifyLink,
                    "expiryHours", String.valueOf(verificationExpiryHours),
                    "logoUrl", logoUrl,
                    "supportEmail", supportEmail,
                    "websiteUrl", websiteUrl,
                    "currentYear", String.valueOf(LocalDateTime.now().getYear())
            );

            String htmlContent = processTemplate(verificationEmailTemplate, variables);
            sendHtmlEmail(email, subject, htmlContent);
            log.info("Verification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", email, e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String email, String token, String firstName) {
        try {
            doSendPasswordResetEmail(email, token, firstName);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
        }
    }

    @Override
    public void sendPasswordResetEmailSync(String email, String token, String firstName) throws EmailSendException {
        try {
            doSendPasswordResetEmail(email, token, firstName);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw EmailSendException.failedToSend("password reset", e);
        } catch (Exception e) {
            log.error("Unexpected error sending password reset email to: {}", email, e);
            throw EmailSendException.failedToSend("password reset", e);
        }
    }

    /**
     * Internal method to send password reset email.
     */
    private void doSendPasswordResetEmail(String email, String token, String firstName) throws MessagingException {
        String subject = "🔑 Reset Your LifePill Password";
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        Map<String, String> variables = Map.of(
                "firstName", firstName,
                "resetLink", resetLink,
                "expiryHours", String.valueOf(passwordResetExpiryHours),
                "logoUrl", logoUrl,
                "supportEmail", supportEmail,
                "websiteUrl", websiteUrl,
                "currentYear", String.valueOf(LocalDateTime.now().getYear())
        );

        String htmlContent = processTemplate(passwordResetEmailTemplate, variables);
        sendHtmlEmail(email, subject, htmlContent);
        log.info("Password reset email sent to: {}", email);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String email, String firstName) {
        try {
            String subject = "🎉 Welcome to LifePill - Locating Hope!";

            Map<String, String> variables = Map.of(
                    "firstName", firstName,
                    "appUrl", frontendUrl,
                    "logoUrl", logoUrl,
                    "supportEmail", supportEmail,
                    "websiteUrl", websiteUrl,
                    "currentYear", String.valueOf(LocalDateTime.now().getYear())
            );

            String htmlContent = processTemplate(welcomeEmailTemplate, variables);
            sendHtmlEmail(email, subject, htmlContent);
            log.info("Welcome email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", email, e);
        }
    }

    @Override
    public void testEmailConfiguration(String email) throws EmailSendException {
        try {
            log.info("Testing email configuration by sending test email to: {}", email);

            String subject = "🧪 LifePill Email Configuration Test";

            Map<String, String> variables = Map.of(
                    "firstName", "User",
                    "verifyLink", frontendUrl,
                    "expiryHours", "24",
                    "logoUrl", logoUrl,
                    "supportEmail", supportEmail,
                    "websiteUrl", websiteUrl,
                    "currentYear", String.valueOf(LocalDateTime.now().getYear())
            );

            String htmlContent = processTemplate(verificationEmailTemplate, variables);
            sendHtmlEmail(email, subject, htmlContent);
            log.info("Test email sent successfully to: {}", email);
        } catch (MailException | MessagingException e) {
            log.error("Email configuration test failed: {}", e.getMessage(), e);
            throw new EmailSendException("Email configuration test failed: " + e.getMessage(), e);
        }
    }

    /**
     * Send HTML email using MimeMessage.
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = isHtml

        mailSender.send(message);
    }
}
