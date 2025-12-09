package com.lifepill.user_auth.controller;

import com.lifepill.user_auth.config.RateLimiterConfig;
import com.lifepill.user_auth.dto.request.*;
import com.lifepill.user_auth.dto.response.ApiResponse;
import com.lifepill.user_auth.dto.response.AuthResponse;
import com.lifepill.user_auth.dto.response.TokenResponse;
import com.lifepill.user_auth.exception.RateLimitExceededException;
import com.lifepill.user_auth.service.AuthService;
import com.lifepill.user_auth.service.EmailTemplateService;
import com.lifepill.user_auth.service.GoogleOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * Handles user registration, login, password reset, and token management.
 */
@Slf4j
@RestController
@RequestMapping("/${api.version:v1}/user/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints for user management")
public class AuthController {

    private final AuthService authService;
    private final RateLimiterConfig rateLimiterConfig;
    private final GoogleOAuthService googleOAuthService;
    private final EmailTemplateService emailTemplateService;

    /**
     * Register a new user.
     *
     * @param request the registration request
     * @param httpRequest the HTTP request for rate limiting
     * @return the authentication response
     */
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with email verification. Returns JWT tokens upon successful registration."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or validation error"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email already exists"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "Too many registration attempts"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = getClientIp(httpRequest);
        
        // Dual-layer rate limiting: IP + Email
        if (!rateLimiterConfig.allowRegisterAttempt(clientIp, request.getEmail())) {
            log.warn("Rate limit exceeded for registration from IP: {} or email: {}", clientIp, request.getEmail());
            throw new RateLimitExceededException("Too many registration attempts. Please try again later.");
        }

        log.info("Registration request received for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    /**
     * Authenticate a user.
     *
     * @param request the login request
     * @param httpRequest the HTTP request for rate limiting
     * @return the authentication response
     */
    @Operation(
            summary = "Login with email and password",
            description = "Authenticates a user with email and password. Returns JWT access and refresh tokens."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "Too many login attempts"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = getClientIp(httpRequest);
        
        // Dual-layer rate limiting: IP + Email
        if (!rateLimiterConfig.allowLoginAttempt(clientIp, request.getEmail())) {
            log.warn("Rate limit exceeded for login from IP: {} or email: {}", clientIp, request.getEmail());
            throw new RateLimitExceededException("Too many login attempts. Please try again later.");
        }

        log.info("Login request received for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Authenticate user via Google Sign-In.
     *
     * @param request the Google sign-in request
     * @param httpRequest the HTTP request for rate limiting
     * @return the authentication response
     */
    @Operation(
            summary = "Sign in with Google",
            description = "Authenticates a user using Google OAuth2 ID token. Creates account if user doesn't exist."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Google Sign-In successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid Google ID token"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "Too many login attempts"
            )
    })
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleSignIn(
            @Valid @RequestBody GoogleSignInRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = getClientIp(httpRequest);
        
        // For Google Sign-In, we only have IP until token is verified
        // Using a placeholder for email check - actual email comes from Google token
        if (!rateLimiterConfig.allowLoginAttempt(clientIp, "google-oauth:" + clientIp)) {
            log.warn("Rate limit exceeded for Google Sign-In from IP: {}", clientIp);
            throw new RateLimitExceededException("Too many login attempts. Please try again later.");
        }

        log.info("Google Sign-In request received");
        AuthResponse response = authService.googleSignIn(request);
        
        return ResponseEntity.ok(ApiResponse.success("Google Sign-In successful", response));
    }

    /**
     * Check if Google Sign-In is enabled.
     *
     * @return status of Google OAuth configuration
     */
    @Operation(
            summary = "Check Google Sign-In status",
            description = "Returns whether Google Sign-In is enabled and configured on the server"
    )
    @GetMapping("/google/status")
    public ResponseEntity<ApiResponse<Boolean>> googleSignInStatus() {
        boolean enabled = googleOAuthService.isEnabled();
        return ResponseEntity.ok(ApiResponse.success(
                enabled ? "Google Sign-In is enabled" : "Google Sign-In is disabled",
                enabled
        ));
    }

    /**
     * Request password reset.
     *
     * @param request the forgot password request
     * @return success response
     */
    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset email to the specified email address if it exists"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password reset email sent"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Email not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "Too many password reset attempts"
            )
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = getClientIp(httpRequest);
        
        // Dual-layer rate limiting: IP + Email
        if (!rateLimiterConfig.allowPasswordResetAttempt(clientIp, request.getEmail())) {
            log.warn("Rate limit exceeded for password reset from IP: {} or email: {}", clientIp, request.getEmail());
            throw new RateLimitExceededException("Too many password reset attempts. Please try again later.");
        }

        log.info("Forgot password request received for email: {}", request.getEmail());
        authService.forgotPassword(request);
        
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent. Please check your inbox."));
    }

/**
 * Serve reset password HTML page.
 * Reads the static HTML file and serves it directly to avoid redirect issues through API Gateway.
 * 
 * @param token the reset token (preserved in URL for JavaScript to use)
 * @return the HTML content
 */
@GetMapping(value = "/reset-password", produces = "text/html")
public ResponseEntity<String> resetPasswordPage(@RequestParam String token) {
    try {
        // Read the static HTML file from classpath
        org.springframework.core.io.Resource resource = 
            new org.springframework.core.io.ClassPathResource("static/reset-password.html");
        
        String htmlContent;
        try (java.io.InputStream inputStream = resource.getInputStream()) {
            htmlContent = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.TEXT_HTML)
                .body(htmlContent);
    } catch (Exception e) {
        log.error("Failed to load reset password page", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("<html><body><h1>Error loading page</h1><p>Please try again later.</p></body></html>");
    }
}

    /**
     * Reset password using token.
     *
     * @param request the reset password request
     * @return success response
     */
    @Operation(
            summary = "Reset password",
            description = "Resets the user's password using the token received via email"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password reset successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired token"
            )
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        log.info("Reset password request received");
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
        } catch (com.lifepill.user_auth.exception.InvalidTokenException e) {
            log.warn("Invalid token during password reset: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("400", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to reset password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("500", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Verify email using token.
     *
     * @param request the verify email request
     * @return success response
     */
    @Operation(
            summary = "Verify email address",
            description = "Verifies the user's email address using the token received via email"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email verified successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired verification token"
            )
    })
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        log.info("Email verification request received");
        authService.verifyEmail(request);
        
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully"));
    }

    /**
     * Verify email using token from query parameter (for email links).
     *
     * @param token the verification token
     * @return success response
     */
    @Operation(
            summary = "Verify email via link",
            description = "Verifies email using token from query parameter (used for email verification links)"
    )
    @GetMapping(value = "/verify-email", produces = "text/html")
    public ResponseEntity<String> verifyEmailViaLink(
            @Parameter(description = "Email verification token") @RequestParam String token
    ) {
        log.info("Email verification request received via GET");
        try {
            VerifyEmailRequest request = new VerifyEmailRequest(token);
            authService.verifyEmail(request);
            
            // Generate success HTML using template service
            String html = emailTemplateService.generateEmailVerificationSuccess();
            
            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(html);
                    
        } catch (Exception e) {
            log.error("Email verification failed: {}", e.getMessage());
            
            // Generate error HTML using template service
            String html = emailTemplateService.generateEmailVerificationError(e.getMessage());
            
            return ResponseEntity.badRequest()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(html);
        }
    }

    /**
     * Resend email verification.
     *
     * @param request the resend verification request
     * @return success response
     */
    @Operation(
            summary = "Resend verification email",
            description = "Resends the email verification link to the specified email address"
    )
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request
    ) {
        log.info("Resend verification request received for email: {}", request.getEmail());
        authService.resendVerification(request);
        
        return ResponseEntity.ok(ApiResponse.success("Verification email sent. Please check your inbox."));
    }

    /**
     * Refresh access token.
     *
     * @param request the refresh token request
     * @return the new token response
     */
    @Operation(
            summary = "Refresh access token",
            description = "Generates new access and refresh tokens using a valid refresh token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token"
            )
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.info("Token refresh request received");
        TokenResponse response = authService.refreshToken(request);
        
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    /**
     * Logout user.
     *
     * @param request the logout request
     * @return success response
     */
    @Operation(
            summary = "Logout user",
            description = "Invalidates the refresh token and logs out the user"
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        log.info("Logout request received");
        authService.logout(request);
        
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    /**
     * Extract client IP address from request.
     * Handles proxy headers for accurate IP detection.
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
