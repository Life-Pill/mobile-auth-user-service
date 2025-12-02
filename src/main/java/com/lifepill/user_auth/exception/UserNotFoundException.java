package com.lifepill.user_auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user is not found.
 */
public class UserNotFoundException extends BaseException {

    private static final String ERROR_CODE = "USER_NOT_FOUND";
    private static final String DEFAULT_MESSAGE = "User not found";

    public UserNotFoundException() {
        super(DEFAULT_MESSAGE, ERROR_CODE, HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String userId) {
        super("User not found with ID: " + userId, ERROR_CODE, HttpStatus.NOT_FOUND);
    }
}
