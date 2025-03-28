package org.kopingenieria.exception.exceptions;

public class ConfigurationException extends Exception {
    public ConfigurationException(String mensaje) {
        super(mensaje);
    }
    public ConfigurationException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
