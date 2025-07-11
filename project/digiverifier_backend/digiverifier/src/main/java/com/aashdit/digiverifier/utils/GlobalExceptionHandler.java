package com.aashdit.digiverifier.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Catch all exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        // Print stack trace to console
        ex.printStackTrace();

        // Log the exception (optional, but recommended)
        logger.error("Unhandled exception caught: ", ex);

        // You can return a custom error response here
        // For demonstration, returning the exception message and stack trace
        StringBuilder sb = new StringBuilder();
        sb.append("Exception: ").append(ex.getClass().getName()).append("\n");
        sb.append("Message: ").append(ex.getMessage()).append("\n");

        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }

        return new ResponseEntity<>(sb.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

