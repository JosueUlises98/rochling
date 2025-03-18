package org.kopingenieria.logging.exception;

public class LogWriteException extends RuntimeException{
    public LogWriteException(String message) {
        super(message);
    }
    public LogWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
