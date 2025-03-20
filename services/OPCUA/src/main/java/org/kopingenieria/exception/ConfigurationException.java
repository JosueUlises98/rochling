package org.kopingenieria.exception;

public class ConfigurationException extends Exception {
    public ConfigurationException(String mensaje) {
        super(mensaje);
    }
    public ConfigurationException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
