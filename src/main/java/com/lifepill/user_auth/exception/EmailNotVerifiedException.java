package com.lifepill.user_auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when email is not verified.
 */
public class EmailNotVerifiedException extends BaseException {

    private static final String ERROR_CODE = "EMAIL_NOT_VERIFIED";
    private static final String DEFAULT_MESSAGE = "Please verify your email before logging in";

    public EmailNotVerifiedException() {
        super(DEFAULT_MESSAGE, ERROR_CODE, HttpStatus.FORBIDDEN);
    }

    public EmailNotVerifiedException(String message) {
        super(message, ERROR_CODE, HttpStatus.FORBIDDEN);
    }
}
