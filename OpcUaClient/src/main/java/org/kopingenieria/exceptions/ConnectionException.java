package org.kopingenieria.exceptions;

public class ConnectionException extends Exception {
    public ConnectionException(String mensaje) {
        super(mensaje);
    }
    public ConnectionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
