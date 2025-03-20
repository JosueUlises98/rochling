package org.kopingenieria.exception;

public class SerializationException extends Exception{
    public SerializationException(String mensaje) {
        super(mensaje);
    }
    public SerializationException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
