package org.kopingenieria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(LogWriteException.class)
    public ResponseEntity<String> handleLogWriteException(LogWriteException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while writing the log: " + ex.getMessage());
    }
    
    @ExceptionHandler(LogWriteFailureEvent.class)
    public ResponseEntity<String> handleLogWriteFailureEvent(LogWriteFailureEvent ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("A log write failure event occurred: " + ex.getMessage());
    }

    @ExceptionHandler(LogReadException.class)
    public ResponseEntity<String> handleLogReadException(LogReadException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while reading the log: " + ex.getMessage());
    }
}
