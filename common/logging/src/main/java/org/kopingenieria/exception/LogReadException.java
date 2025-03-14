package org.kopingenieria.exception;

public class LogReadException extends Exception {
    public LogReadException(String message) {
        super(message);
    }
    public LogReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
