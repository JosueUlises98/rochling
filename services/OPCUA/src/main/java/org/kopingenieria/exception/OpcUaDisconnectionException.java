package org.kopingenieria.exception;

public class OpcUaDisconnectionException extends OpcUaConnectionException {
    public OpcUaDisconnectionException(String mensaje) {
        super(mensaje);
    }
    public OpcUaDisconnectionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
