package com.lifepill.user_auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for generating HTML email templates.
 * Provides pre-designed, responsive HTML pages for email verification, password reset, etc.
 */
@Service
public class EmailTemplateService {

    @Value("${app.branding.logo-url:}")
    private String logoUrl;

    /**
     * Generates HTML page for successful email verification.
     *
     * @return HTML content as String
     */
    public String generateEmailVerificationSuccess() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Email Verified - LifePill</title>
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Helvetica Neue', sans-serif;
                            min-height: 100vh;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            padding: 20px;
                        }
                        
                        .container {
                            background: white;
                            border-radius: 20px;
                            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
                            max-width: 500px;
                            width: 100%;
                            padding: 60px 40px;
                            text-align: center;
                            animation: slideUp 0.6s ease-out;
                        }
                        
                        @keyframes slideUp {
                            from {
                                opacity: 0;
                                transform: translateY(30px);
                            }
                            to {
                                opacity: 1;
                                transform: translateY(0);
                            }
                        }
                        
                        .success-icon {
                            width: 100px;
                            height: 100px;
                            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                            border-radius: 50%;
                            display: inline-flex;
                            align-items: center;
                            justify-content: center;
                            margin-bottom: 30px;
                            box-shadow: 0 10px 30px rgba(16, 185, 129, 0.3);
                            animation: checkmark 0.8s ease-out 0.3s both;
                        }
                        
                        @keyframes checkmark {
                            0% {
                                transform: scale(0);
                            }
                            50% {
                                transform: scale(1.2);
                            }
                            100% {
                                transform: scale(1);
                            }
                        }
                        
                        .checkmark {
                            width: 50px;
                            height: 50px;
                            border: 5px solid white;
                            border-top: none;
                            border-right: none;
                            transform: rotate(-45deg);
                            margin-top: 15px;
                        }
                        
                        h1 {
                            color: #1f2937;
                            font-size: 2.5rem;
                            font-weight: 700;
                            margin-bottom: 16px;
                            line-height: 1.2;
                        }
                        
                        .subtitle {
                            color: #10b981;
                            font-size: 1.1rem;
                            font-weight: 600;
                            margin-bottom: 24px;
                        }
                        
                        p {
                            color: #6b7280;
                            font-size: 1.05rem;
                            line-height: 1.7;
                            margin-bottom: 32px;
                        }
                        
                        .features {
                            background: #f9fafb;
                            border-radius: 12px;
                            padding: 24px;
                            margin-bottom: 32px;
                            text-align: left;
                        }
                        
                        .feature-item {
                            display: flex;
                            align-items: center;
                            margin-bottom: 12px;
                            color: #374151;
                        }
                        
                        .feature-item:last-child {
                            margin-bottom: 0;
                        }
                        
                        .feature-icon {
                            width: 24px;
                            height: 24px;
                            background: #10b981;
                            border-radius: 50%;
                            display: inline-flex;
                            align-items: center;
                            justify-content: center;
                            margin-right: 12px;
                            flex-shrink: 0;
                        }
                        
                        .feature-icon::after {
                            content: '✓';
                            color: white;
                            font-weight: bold;
                            font-size: 14px;
                        }
                        
                        .button {
                            display: inline-block;
                            padding: 16px 48px;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white;
                            text-decoration: none;
                            border-radius: 12px;
                            font-weight: 600;
                            font-size: 1.1rem;
                            transition: all 0.3s ease;
                            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
                        }
                        
                        .button:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
                        }
                        
                        .button:active {
                            transform: translateY(0);
                        }
                        
                        @media (max-width: 600px) {
                            .container {
                                padding: 40px 24px;
                            }
                            
                            h1 {
                                font-size: 2rem;
                            }
                            
                            .button {
                                width: 100%;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="success-icon">
                            <div class="checkmark"></div>
                        </div>
                        
                        <h1>Email Verified!</h1>
                        <p class="subtitle">Welcome to LifePill</p>
                        
                        <p>Your email has been successfully verified. You now have full access to all LifePill features.</p>
                        
                        <div class="features">
                            <div class="feature-item">
                                <div class="feature-icon"></div>
                                <span>Access your health records</span>
                            </div>
                            <div class="feature-item">
                                <div class="feature-icon"></div>
                                <span>Order medications online</span>
                            </div>
                            <div class="feature-item">
                                <div class="feature-icon"></div>
                                <span>Track prescriptions &amp; refills</span>
                            </div>
                            <div class="feature-item">
                                <div class="feature-icon"></div>
                                <span>Connect with pharmacies</span>
                            </div>
                        </div>
                        
                        <a href="lifepill://verified" class="button">Open LifePill App</a>
                    </div>
                </body>
                </html>
                """;
    }

    /**
     * Generates HTML page for failed email verification.
     *
     * @param errorMessage the error message to display
     * @return HTML content as String
     */
    public String generateEmailVerificationError(String errorMessage) {
        String safeErrorMessage = escapeHtml(errorMessage);
        
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Verification Failed - LifePill</title>
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Helvetica Neue', sans-serif;
                            min-height: 100vh;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            padding: 20px;
                        }
                        
                        .container {
                            background: white;
                            border-radius: 20px;
                            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
                            max-width: 500px;
                            width: 100%;
                            padding: 60px 40px;
                            text-align: center;
                            animation: slideUp 0.6s ease-out;
                        }
                        
                        @keyframes slideUp {
                            from {
                                opacity: 0;
                                transform: translateY(30px);
                            }
                            to {
                                opacity: 1;
                                transform: translateY(0);
                            }
                        }
                        
                        .error-icon {
                            width: 100px;
                            height: 100px;
                            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
                            border-radius: 50%;
                            display: inline-flex;
                            align-items: center;
                            justify-content: center;
                            margin-bottom: 30px;
                            box-shadow: 0 10px 30px rgba(239, 68, 68, 0.3);
                            position: relative;
                            animation: shake 0.5s ease-out 0.3s;
                        }
                        
                        @keyframes shake {
                            0%, 100% { transform: translateX(0); }
                            25% { transform: translateX(-10px); }
                            75% { transform: translateX(10px); }
                        }
                        
                        .error-icon::before,
                        .error-icon::after {
                            content: '';
                            position: absolute;
                            width: 5px;
                            height: 50px;
                            background: white;
                            border-radius: 3px;
                        }
                        
                        .error-icon::before {
                            transform: rotate(45deg);
                        }
                        
                        .error-icon::after {
                            transform: rotate(-45deg);
                        }
                        
                        h1 {
                            color: #1f2937;
                            font-size: 2.5rem;
                            font-weight: 700;
                            margin-bottom: 16px;
                            line-height: 1.2;
                        }
                        
                        .subtitle {
                            color: #ef4444;
                            font-size: 1.1rem;
                            font-weight: 600;
                            margin-bottom: 24px;
                        }
                        
                        p {
                            color: #6b7280;
                            font-size: 1.05rem;
                            line-height: 1.7;
                            margin-bottom: 24px;
                        }
                        
                        .error-details {
                            background: #fef2f2;
                            border-left: 4px solid #ef4444;
                            border-radius: 8px;
                            padding: 20px;
                            margin-bottom: 24px;
                            text-align: left;
                        }
                        
                        .error-label {
                            color: #991b1b;
                            font-weight: 600;
                            font-size: 0.9rem;
                            margin-bottom: 8px;
                            display: block;
                        }
                        
                        .error-message {
                            color: #dc2626;
                            font-size: 0.95rem;
                            font-family: 'Courier New', monospace;
                            word-break: break-word;
                        }
                        
                        .help-text {
                            background: #f9fafb;
                            border-radius: 12px;
                            padding: 20px;
                            margin-bottom: 32px;
                        }
                        
                        .help-text strong {
                            color: #374151;
                            display: block;
                            margin-bottom: 12px;
                        }
                        
                        .help-text ul {
                            list-style: none;
                            padding: 0;
                            text-align: left;
                        }
                        
                        .help-text li {
                            color: #6b7280;
                            padding-left: 24px;
                            position: relative;
                            margin-bottom: 8px;
                        }
                        
                        .help-text li::before {
                            content: '•';
                            color: #667eea;
                            font-weight: bold;
                            position: absolute;
                            left: 8px;
                        }
                        
                        .button {
                            display: inline-block;
                            padding: 16px 48px;
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white;
                            text-decoration: none;
                            border-radius: 12px;
                            font-weight: 600;
                            font-size: 1.1rem;
                            transition: all 0.3s ease;
                            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
                        }
                        
                        .button:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
                        }
                        
                        @media (max-width: 600px) {
                            .container {
                                padding: 40px 24px;
                            }
                            
                            h1 {
                                font-size: 2rem;
                            }
                            
                            .button {
                                width: 100%;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="error-icon"></div>
                        
                        <h1>Verification Failed</h1>
                        <p class="subtitle">Unable to verify your email</p>
                        
                        <p>We couldn't verify your email address. The verification link may have expired or is invalid.</p>
                        
                        <div class="error-details">
                            <span class="error-label">Error Details:</span>
                            <div class="error-message">%s</div>
                        </div>
                        
                        <div class="help-text">
                            <strong>What you can do:</strong>
                            <ul>
                                <li>Request a new verification email from the app</li>
                                <li>Check your spam/junk folder for the email</li>
                                <li>Contact support if the issue persists</li>
                            </ul>
                        </div>
                        
                        <a href="lifepill://home" class="button">Back to LifePill</a>
                    </div>
                </body>
                </html>
                """.formatted(safeErrorMessage);
    }

    /**
     * Escapes HTML special characters to prevent XSS attacks.
     *
     * @param text the text to escape
     * @return escaped text
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "Unknown error";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
}
