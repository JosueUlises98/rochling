package org.kopingenieria.exception.exceptions;

public class ConfigurationMappingException extends Exception {
    public ConfigurationMappingException(String mensaje) {
        super(mensaje);
    }
    public ConfigurationMappingException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
