package org.kopingenieria.exception.exceptions;

public class DisconnectException extends Exception {
    public DisconnectException(String mensaje) {
        super(mensaje);
    }
    public DisconnectException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
