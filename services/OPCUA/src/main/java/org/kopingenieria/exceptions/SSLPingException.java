package org.kopingenieria.exceptions;

public class SSLPingException extends SSLConnectionException {
    public SSLPingException(String mensaje) {
        super(mensaje);
    }
    public SSLPingException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
