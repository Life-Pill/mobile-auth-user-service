package com.lifepill.user_auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a token (reset, verification, refresh) is invalid or expired.
 */
public class InvalidTokenException extends BaseException {

    private static final String ERROR_CODE = "INVALID_TOKEN";
    private static final String DEFAULT_MESSAGE = "Invalid or expired token";

    public InvalidTokenException() {
        super(DEFAULT_MESSAGE, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public InvalidTokenException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public static InvalidTokenException expiredResetToken() {
        return new InvalidTokenException("Invalid or expired reset token");
    }

    public static InvalidTokenException expiredVerificationToken() {
        return new InvalidTokenException("Invalid or expired verification token");
    }

    public static InvalidTokenException invalidRefreshToken() {
        return new InvalidTokenException("Invalid or expired refresh token");
    }
}
