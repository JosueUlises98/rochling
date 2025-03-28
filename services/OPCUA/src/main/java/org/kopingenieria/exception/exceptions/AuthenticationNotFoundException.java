package org.kopingenieria.exception.exceptions;

public class AuthenticationNotFoundException extends AuthenticationException {
    public AuthenticationNotFoundException(String mensaje) {
        super(mensaje);
    }
    public AuthenticationNotFoundException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
