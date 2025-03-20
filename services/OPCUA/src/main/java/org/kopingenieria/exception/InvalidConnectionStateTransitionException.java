package org.kopingenieria.exception;

public class InvalidConnectionStateTransitionException extends ConnectionException{
    public InvalidConnectionStateTransitionException(String mensaje) {
        super(mensaje);
    }
    public InvalidConnectionStateTransitionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
