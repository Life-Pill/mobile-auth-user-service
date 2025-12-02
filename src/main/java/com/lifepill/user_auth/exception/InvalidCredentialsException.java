package com.lifepill.user_auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when login credentials are invalid.
 */
public class InvalidCredentialsException extends BaseException {

    private static final String ERROR_CODE = "INVALID_CREDENTIALS";
    private static final String DEFAULT_MESSAGE = "Invalid email or password";

    public InvalidCredentialsException() {
        super(DEFAULT_MESSAGE, ERROR_CODE, HttpStatus.UNAUTHORIZED);
    }

    public InvalidCredentialsException(String message) {
        super(message, ERROR_CODE, HttpStatus.UNAUTHORIZED);
    }
}
