package org.kopingenieria.exception.exceptions;

public class ConnectionPoolException extends PoolException {
    public ConnectionPoolException(String mensaje) {
        super(mensaje);
    }
    public ConnectionPoolException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
