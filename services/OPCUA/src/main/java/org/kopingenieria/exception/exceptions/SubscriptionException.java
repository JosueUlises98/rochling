package org.kopingenieria.exception.exceptions;

public class SubscriptionException extends Exception {
    public SubscriptionException(String mensaje) {
        super(mensaje);
    }
    public SubscriptionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
