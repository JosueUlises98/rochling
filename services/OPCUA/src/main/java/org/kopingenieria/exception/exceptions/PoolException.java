package org.kopingenieria.exception.exceptions;

public class PoolException extends Exception {
    public PoolException(String mensaje) {
        super(mensaje);
    }
    public PoolException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
