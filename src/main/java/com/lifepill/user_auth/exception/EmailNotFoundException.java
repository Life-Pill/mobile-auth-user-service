package com.lifepill.user_auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when email is not found.
 */
public class EmailNotFoundException extends BaseException {

    private static final String ERROR_CODE = "EMAIL_NOT_FOUND";
    private static final String DEFAULT_MESSAGE = "No account found with this email address";

    public EmailNotFoundException() {
        super(DEFAULT_MESSAGE, ERROR_CODE, HttpStatus.NOT_FOUND);
    }

    public EmailNotFoundException(String email) {
        super("No account found with email: " + email, ERROR_CODE, HttpStatus.NOT_FOUND);
    }
}
