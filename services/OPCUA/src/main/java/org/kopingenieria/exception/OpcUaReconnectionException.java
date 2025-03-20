package org.kopingenieria.exception;


public class OpcUaReconnectionException extends OpcUaConnectionException {
    public OpcUaReconnectionException(String mensaje) {
        super(mensaje);
    }
    public OpcUaReconnectionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
