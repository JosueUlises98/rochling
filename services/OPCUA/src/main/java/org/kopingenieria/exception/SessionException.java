package org.kopingenieria.exception;

public class SessionException extends Exception {
    public SessionException(String mensaje) {
        super(mensaje);
    }
    public SessionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
