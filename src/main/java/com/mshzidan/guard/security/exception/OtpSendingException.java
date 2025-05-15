package com.mshzidan.guard.security.exception;

public class OtpSendingException extends RuntimeException{
    public OtpSendingException(String message) {
        super(message);
    }

    public OtpSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
