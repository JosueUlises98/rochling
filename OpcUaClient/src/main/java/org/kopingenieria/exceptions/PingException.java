package org.kopingenieria.exceptions;

public class PingException extends Exception {
    public PingException(String mensaje) {
        super(mensaje);
    }
    public PingException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
