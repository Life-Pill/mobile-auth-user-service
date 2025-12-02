package com.lifepill.user_auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when email already exists during registration.
 */
public class EmailAlreadyExistsException extends BaseException {

    private static final String ERROR_CODE = "EMAIL_ALREADY_EXISTS";
    private static final String DEFAULT_MESSAGE = "An account with this email already exists";

    public EmailAlreadyExistsException() {
        super(DEFAULT_MESSAGE, ERROR_CODE, HttpStatus.CONFLICT);
    }

    public EmailAlreadyExistsException(String email) {
        super("An account with email " + email + " already exists", ERROR_CODE, HttpStatus.CONFLICT);
    }
}
