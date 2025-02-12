package org.kopingenieria.exceptions;

public class SuscriptionException extends Exception{
    public SuscriptionException(String message) {
        super(message);
    }
    public SuscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
