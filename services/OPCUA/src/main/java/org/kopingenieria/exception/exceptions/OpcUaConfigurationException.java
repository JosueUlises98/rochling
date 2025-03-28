package org.kopingenieria.exception.exceptions;

public class OpcUaConfigurationException extends Exception {
    public OpcUaConfigurationException(String mensaje) {
        super(mensaje);
    }
    public OpcUaConfigurationException(String mensaje, Throwable cause) {
        super(mensaje, cause);
    }
}
