package com.lifepill.user_auth.service.impl;

import com.lifepill.user_auth.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of EmailService for sending professional HTML emails.
 * Handles verification, password reset, and welcome emails with LifePill branding.
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

    // LifePill Brand Colors
    private static final String PRIMARY_COLOR = "#00B4D8";      // Cyan blue
    private static final String PRIMARY_GRADIENT_START = "#00B4D8";
    private static final String PRIMARY_GRADIENT_END = "#0077B6";
    private static final String TEXT_COLOR = "#333333";
    private static final String SECONDARY_TEXT = "#666666";
    private static final String BACKGROUND_COLOR = "#F8FAFC";
    private static final String WHITE = "#FFFFFF";

    // LifePill Logo (Base64 encoded or URL)
    private static final String LOGO_URL = "https://raw.githubusercontent.com/Life-Pill/mobile-auth-user-service/main/assets/lifepill-logo.png";
    
    // LifePill Logo as Base64 SVG - Heart with plus sign in gradient circle (matching brand)
    private static final String LOGO_BASE64 = "data:image/svg+xml;base64," +
            "PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjEyMCIgdmlld0JveD0iMCAwIDEyMCAxMjAiIGZpbGw9" +
            "Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxkZWZzPgo8bGluZWFy" +
            "R3JhZGllbnQgaWQ9ImJnR3JhZCIgeDE9IjAlIiB5MT0iMCUiIHgyPSIxMDAlIiB5Mj0iMTAwJSI+" +
            "CjxzdG9wIG9mZnNldD0iMCUiIHN0b3AtY29sb3I9IiM0RkQxQzUiLz4KPHN0b3Agb2Zmc2V0PSIx" +
            "MDAlIiBzdG9wLWNvbG9yPSIjMDA5NEVGII8vPgo8L2xpbmVhckdyYWRpZW50Pgo8L2RlZnM+Cjxj" +
            "aXJjbGUgY3g9IjYwIiBjeT0iNjAiIHI9IjU4IiBmaWxsPSJ1cmwoI2JnR3JhZCkiLz4KPHBhdGgg" +
            "ZD0iTTYwIDI4QzQ3IDI4IDM4IDM4IDM4IDUwQzM4IDY4IDYwIDkyIDYwIDkyQzYwIDkyIDgyIDY4" +
            "IDgyIDUwQzgyIDM4IDczIDI4IDYwIDI4WiIgZmlsbD0id2hpdGUiLz4KPHJlY3QgeD0iNTYiIHk9" +
            "IjEwIiB3aWR0aD0iOCIgaGVpZ2h0PSIyMCIgcng9IjQiIGZpbGw9IndoaXRlIi8+CjxyZWN0IHg9" +
            "IjUwIiB5PSIxNiIgd2lkdGg9IjIwIiBoZWlnaHQ9IjgiIHJ4PSI0IiBmaWxsPSJ3aGl0ZSIvPgo8" +
            "L3N2Zz4=";

    @Override
    @Async
    public void sendVerificationEmail(String email, String token, String firstName) {
        try {
            String subject = "✉️ Verify Your LifePill Account";
            String verifyLink = verificationUrl + "?token=" + token;
            
            String htmlContent = buildEmailTemplate(
                    firstName,
                    "Verify Your Email",
                    "Welcome to LifePill! We're excited to have you on board. Please verify your email address to activate your account and start your journey with us.",
                    verifyLink,
                    "Verify Email Address",
                    "This verification link will expire in <strong>24 hours</strong>.",
                    "If you didn't create an account with LifePill, you can safely ignore this email.",
                    ""
            );

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
            String subject = "Reset Your LifePill Password";
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            
            String htmlContent = buildEmailTemplate(
                    firstName,
                    "Reset Your Password",
                    "We received a request to reset the password for your LifePill account. Click the button below to create a new password.",
                    resetLink,
                    "Reset Password",
                    "This password reset link will expire in <strong>1 hour</strong> for security reasons.",
                    "If you didn't request a password reset, please ignore this email. Your password will remain unchanged and your account is secure.",
                    ""
            );

            sendHtmlEmail(email, subject, htmlContent);
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(String email, String firstName) {
        try {
            String subject = " Welcome to LifePill - Locating Hope!";
            
            String htmlContent = buildWelcomeEmailTemplate(firstName);

            sendHtmlEmail(email, subject, htmlContent);
            log.info("Welcome email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", email, e);
        }
    }

    /**
     * Build the main email template with LifePill branding.
     */
    private String buildEmailTemplate(
            String firstName,
            String heading,
            String message,
            String buttonLink,
            String buttonText,
            String expiryNote,
            String securityNote,
            String emoji
    ) {
        String currentYear = String.valueOf(LocalDateTime.now().getYear());
        
        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>LifePill - %s</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: %s;">
                    <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                        <tr>
                            <td style="padding: 40px 0;">
                                <table role="presentation" style="width: 100%%; max-width: 600px; margin: 0 auto; background-color: %s; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);">
                                    
                                    <!-- Header with Logo -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, %s 0%%, %s 100%%); padding: 40px 40px 30px 40px; text-align: center;">
                                            <!-- LifePill Logo -->
                                            <div style="margin: 0 auto 20px auto;">
                                                <img src="%s" alt="LifePill Logo" width="100" height="100" style="display: block; margin: 0 auto; border-radius: 50%%; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);" />
                                            </div>
                                            <!-- Brand Name -->
                                            <h1 style="margin: 0; font-size: 32px; font-weight: 700; letter-spacing: 1px;">
                                                <span style="color: %s;">LIFE</span><span style="color: #1E3A5F;">PILL</span>
                                            </h1>
                                            <p style="margin: 8px 0 0 0; color: rgba(255, 255, 255, 0.95); font-size: 13px; letter-spacing: 3px; text-transform: uppercase; font-weight: 500;">
                                                Locating Hope
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Main Content -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <!-- Greeting -->
                                            <h2 style="margin: 0 0 8px 0; color: %s; font-size: 24px; font-weight: 600;">
                                                Hello, %s! %s
                                            </h2>
                                            <h3 style="margin: 0 0 24px 0; color: %s; font-size: 18px; font-weight: 500;">
                                                %s
                                            </h3>
                                            
                                            <!-- Message -->
                                            <p style="margin: 0 0 32px 0; color: %s; font-size: 15px; line-height: 1.7;">
                                                %s
                                            </p>
                                            
                                            <!-- CTA Button -->
                                            <table role="presentation" style="width: 100%%; margin-bottom: 32px;">
                                                <tr>
                                                    <td style="text-align: center;">
                                                        <a href="%s" style="display: inline-block; padding: 16px 48px; background: linear-gradient(135deg, %s 0%%, %s 100%%); color: %s; text-decoration: none; font-size: 16px; font-weight: 600; border-radius: 50px; box-shadow: 0 4px 16px rgba(0, 180, 216, 0.4); transition: all 0.3s ease;">
                                                            %s
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Link fallback -->
                                            <p style="margin: 0 0 24px 0; color: %s; font-size: 13px; text-align: center;">
                                                Or copy and paste this link in your browser:<br>
                                                <a href="%s" style="color: %s; word-break: break-all;">%s</a>
                                            </p>
                                            
                                            <!-- Expiry Note -->
                                            <div style="background-color: #FFF8E1; border-left: 4px solid #FFC107; padding: 16px; border-radius: 8px; margin-bottom: 24px;">
                                                <p style="margin: 0; color: #856404; font-size: 14px;">
                                                     %s
                                                </p>
                                            </div>
                                            
                                            <!-- Security Note -->
                                            <div style="background-color: #E3F2FD; border-left: 4px solid %s; padding: 16px; border-radius: 8px;">
                                                <p style="margin: 0; color: #1565C0; font-size: 14px;">
                                                     %s
                                                </p>
                                            </div>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #F8FAFC; padding: 32px 40px; border-top: 1px solid #E2E8F0;">
                                            <table role="presentation" style="width: 100%%;">
                                                <tr>
                                                    <td style="text-align: center;">
                                                        <!-- Social Links -->
                                                        <p style="margin: 0 0 16px 0;">
                                                            <a href="#" style="display: inline-block; margin: 0 8px; color: %s; text-decoration: none;">
                                                                <span style="font-size: 20px;">📱</span>
                                                            </a>
                                                            <a href="#" style="display: inline-block; margin: 0 8px; color: %s; text-decoration: none;">
                                                                <span style="font-size: 20px;">💬</span>
                                                            </a>
                                                            <a href="#" style="display: inline-block; margin: 0 8px; color: %s; text-decoration: none;">
                                                                <span style="font-size: 20px;">🌐</span>
                                                            </a>
                                                        </p>
                                                        
                                                        <p style="margin: 0 0 8px 0; color: %s; font-size: 13px;">
                                                            Need help? Contact us at <a href="mailto:lpramithamj@gmail.com" style="color: %s; text-decoration: none;">lpramithamj@gmail.com</a>
                                                        </p>
                                                        
                                                        <p style="margin: 0; color: #94A3B8; font-size: 12px;">
                                                            © %s LifePill. All rights reserved.<br>
                                                            Locating Hope, One Pill at a Time.
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """,
                heading, // title
                BACKGROUND_COLOR, // body bg
                WHITE, // main container bg
                PRIMARY_GRADIENT_START, // header gradient start
                PRIMARY_GRADIENT_END, // header gradient end
                LOGO_BASE64, // logo image src
                WHITE, // LIFE text color
                TEXT_COLOR, // greeting color
                firstName, // name
                emoji, // emoji
                PRIMARY_COLOR, // subheading color
                heading, // subheading text
                SECONDARY_TEXT, // message color
                message, // message text
                buttonLink, // button href
                PRIMARY_GRADIENT_START, // button gradient start
                PRIMARY_GRADIENT_END, // button gradient end
                WHITE, // button text color
                buttonText, // button text
                SECONDARY_TEXT, // link fallback color
                buttonLink, // link href
                PRIMARY_COLOR, // link color
                buttonLink, // link text
                expiryNote, // expiry note
                PRIMARY_COLOR, // security note border
                securityNote, // security note text
                PRIMARY_COLOR, // social icon 1
                PRIMARY_COLOR, // social icon 2
                PRIMARY_COLOR, // social icon 3
                SECONDARY_TEXT, // help text color
                PRIMARY_COLOR, // support email color
                currentYear // copyright year
        );
    }

    /**
     * Build welcome email template with special styling.
     */
    private String buildWelcomeEmailTemplate(String firstName) {
        String currentYear = String.valueOf(LocalDateTime.now().getYear());
        
        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Welcome to LifePill</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: %s;">
                    <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                        <tr>
                            <td style="padding: 40px 0;">
                                <table role="presentation" style="width: 100%%; max-width: 600px; margin: 0 auto; background-color: %s; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);">
                                    
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, %s 0%%, %s 100%%); padding: 50px 40px; text-align: center;">
                                            <!-- LifePill Logo -->
                                            <div style="margin: 0 auto 24px auto;">
                                                <img src="%s" alt="LifePill Logo" width="100" height="100" style="display: block; margin: 0 auto; border-radius: 50%%; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);" />
                                            </div>
                                            <h1 style="margin: 0; font-size: 32px; font-weight: 700;">
                                                <span style="color: %s;">Welcome to LifePill!</span>
                                            </h1>
                                            <p style="margin: 12px 0 0 0; color: rgba(255, 255, 255, 0.95); font-size: 16px;">
                                                Your account has been verified successfully ✨
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <h2 style="margin: 0 0 16px 0; color: %s; font-size: 22px;">
                                                Hi %s! 👋
                                            </h2>
                                            
                                            <p style="margin: 0 0 24px 0; color: %s; font-size: 15px; line-height: 1.7;">
                                                Thank you for verifying your email address. You're now part of the LifePill family, and we're thrilled to have you with us!
                                            </p>
                                            
                                            <!-- Features -->
                                            <div style="background: linear-gradient(135deg, #E0F7FA 0%%, #B2EBF2 100%%); border-radius: 12px; padding: 24px; margin-bottom: 24px;">
                                                <h3 style="margin: 0 0 16px 0; color: %s; font-size: 16px;">
                                                    🚀 What you can do now:
                                                </h3>
                                                <ul style="margin: 0; padding-left: 20px; color: %s; font-size: 14px; line-height: 2;">
                                                    <li>Search for medicines and pharmacies near you</li>
                                                    <li>Set medication reminders</li>
                                                    <li>Track your health history</li>
                                                    <li>Connect with healthcare providers</li>
                                                </ul>
                                            </div>
                                            
                                            <!-- CTA -->
                                            <table role="presentation" style="width: 100%%; margin-bottom: 24px;">
                                                <tr>
                                                    <td style="text-align: center;">
                                                        <a href="%s" style="display: inline-block; padding: 16px 48px; background: linear-gradient(135deg, %s 0%%, %s 100%%); color: %s; text-decoration: none; font-size: 16px; font-weight: 600; border-radius: 50px; box-shadow: 0 4px 16px rgba(0, 180, 216, 0.4);">
                                                            Open LifePill App
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <p style="margin: 0; color: %s; font-size: 14px; text-align: center;">
                                                Have questions? Our support team is always here to help!
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #F8FAFC; padding: 32px 40px; border-top: 1px solid #E2E8F0; text-align: center;">
                                            <p style="margin: 0 0 8px 0; color: %s; font-size: 14px; font-weight: 600;">
                                                <span style="color: %s;">LIFE</span><span style="color: #1E3A5F;">PILL</span>
                                            </p>
                                            <p style="margin: 0 0 16px 0; color: #94A3B8; font-size: 12px; letter-spacing: 1px;">
                                                LOCATING HOPE
                                            </p>
                                            <p style="margin: 0; color: #94A3B8; font-size: 12px;">
                                                © %s LifePill. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                    
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """,
                BACKGROUND_COLOR, // body bg
                WHITE, // container bg
                PRIMARY_GRADIENT_START, // header gradient start
                PRIMARY_GRADIENT_END, // header gradient end
                LOGO_BASE64, // logo image src
                WHITE, // welcome title color
                TEXT_COLOR, // hi greeting color
                firstName, // name
                SECONDARY_TEXT, // paragraph color
                PRIMARY_COLOR, // features heading color
                TEXT_COLOR, // features list color
                frontendUrl, // CTA link
                PRIMARY_GRADIENT_START, // button gradient start
                PRIMARY_GRADIENT_END, // button gradient end
                WHITE, // button text
                SECONDARY_TEXT, // help text
                TEXT_COLOR, // footer brand
                PRIMARY_COLOR, // LIFE color
                currentYear // year
        );
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
