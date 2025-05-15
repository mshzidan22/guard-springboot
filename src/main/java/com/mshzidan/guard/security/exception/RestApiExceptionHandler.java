package com.mshzidan.guard.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestApiExceptionHandler {

    @ExceptionHandler(OtpSendingException.class)
    public ResponseEntity<ErrorResponse> handleOtpSendingException(OtpSendingException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "OTP_SENDING_FAILED",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // Add other REST-specific exceptions here
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
//        ErrorResponse errorResponse = new ErrorResponse(
//                "INTERNAL_SERVER_ERROR",
//                "An unexpected error occurred"
//        );
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//    }
}