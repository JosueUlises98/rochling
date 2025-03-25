package org.kopingenieria.exception;

public class AuthenticationNotFoundException extends Exception {
    public AuthenticationNotFoundException(String mensaje) {
        super(mensaje);
    }
    public AuthenticationNotFoundException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
