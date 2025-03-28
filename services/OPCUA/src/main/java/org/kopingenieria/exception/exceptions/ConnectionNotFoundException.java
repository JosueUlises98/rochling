package org.kopingenieria.exception.exceptions;

public class ConnectionNotFoundException extends Exception {
    public ConnectionNotFoundException(String mensaje) {
        super(mensaje);
    }
    public ConnectionNotFoundException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
