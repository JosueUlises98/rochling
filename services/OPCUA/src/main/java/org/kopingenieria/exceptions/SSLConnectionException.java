package org.kopingenieria.exceptions;

public class SSLConnectionException extends ConnectionException{
    public SSLConnectionException(String mensaje) {
        super(mensaje);
    }
    public SSLConnectionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
