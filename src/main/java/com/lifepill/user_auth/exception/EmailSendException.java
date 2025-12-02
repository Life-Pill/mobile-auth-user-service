package com.lifepill.user_auth.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when email sending fails.
 */
public class EmailSendException extends BaseException {

    private static final String ERROR_CODE = "EMAIL_SEND_FAILED";

    public EmailSendException(String message) {
        super(message, ERROR_CODE, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public EmailSendException(String message, Throwable cause) {
        super(message, ERROR_CODE, HttpStatus.SERVICE_UNAVAILABLE);
        initCause(cause);
    }

    public static EmailSendException failedToSend(String emailType) {
        return new EmailSendException(
                String.format("Failed to send %s email. Please try again later.", emailType)
        );
    }

    public static EmailSendException failedToSend(String emailType, Throwable cause) {
        return new EmailSendException(
                String.format("Failed to send %s email. Please try again later.", emailType),
                cause
        );
    }
}
