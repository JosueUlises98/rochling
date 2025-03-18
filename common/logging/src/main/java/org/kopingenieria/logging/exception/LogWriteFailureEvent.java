package org.kopingenieria.logging.exception;

public class LogWriteFailureEvent extends RuntimeException{
    public LogWriteFailureEvent(String message) {
        super(message);
    }
    public LogWriteFailureEvent(String message, Throwable cause) {
        super(message, cause);
    }
}
