package com.lifepill.user_auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when rate limit is exceeded.
 */
public class RateLimitExceededException extends BaseException {

    private static final String ERROR_CODE = "RATE_LIMIT_EXCEEDED";
    private static final String DEFAULT_MESSAGE = "Too many requests. Please try again later.";

    public RateLimitExceededException() {
        super(DEFAULT_MESSAGE, ERROR_CODE, HttpStatus.TOO_MANY_REQUESTS);
    }

    public RateLimitExceededException(String message) {
        super(message, ERROR_CODE, HttpStatus.TOO_MANY_REQUESTS);
    }
}
