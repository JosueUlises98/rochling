package org.kopingenieria.exceptions;

public class SSLDisconnectionException extends SSLConnectionException{
    public SSLDisconnectionException(String mensaje) {
        super(mensaje);
    }
    public SSLDisconnectionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
