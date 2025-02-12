package org.kopingenieria.exceptions;

public class ReconnectionException extends Exception {
    public ReconnectionException(String mensaje) {
        super(mensaje);
    }
    public ReconnectionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
