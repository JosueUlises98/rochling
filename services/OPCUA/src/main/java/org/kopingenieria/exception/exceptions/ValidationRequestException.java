package org.kopingenieria.exception.exceptions;


public class ValidationRequestException extends Exception {
    public ValidationRequestException(String mensaje) {
        super(mensaje);
    }
    public ValidationRequestException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
