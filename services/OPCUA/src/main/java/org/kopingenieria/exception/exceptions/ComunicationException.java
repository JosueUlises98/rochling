package org.kopingenieria.exception.exceptions;

public class ComunicationException extends Exception {
    public ComunicationException(String mensaje) {
        super(mensaje);
    }
    public ComunicationException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
