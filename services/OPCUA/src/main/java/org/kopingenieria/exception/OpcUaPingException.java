package org.kopingenieria.exception;

public class OpcUaPingException extends OpcUaConnectionException {
    public OpcUaPingException(String mensaje) {
        super(mensaje);
    }
    public OpcUaPingException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
