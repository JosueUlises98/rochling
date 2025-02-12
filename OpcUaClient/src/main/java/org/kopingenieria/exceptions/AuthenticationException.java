package org.kopingenieria.exceptions;

public class AuthenticationException extends Exception {
    public AuthenticationException(String mensaje) {
        super(mensaje);
    }
    public AuthenticationException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
