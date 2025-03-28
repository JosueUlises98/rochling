package org.kopingenieria.exception.exceptions;

public class OpcUaConnectionException extends ConnectionException {
    public OpcUaConnectionException(String mensaje) {
        super(mensaje);
    }
    public OpcUaConnectionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
