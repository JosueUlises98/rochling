package org.kopingenieria.exceptions;

public class SSLReconnectionException extends SSLConnectionException {
    public SSLReconnectionException(String mensaje) {
        super(mensaje);
    }
    public SSLReconnectionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
