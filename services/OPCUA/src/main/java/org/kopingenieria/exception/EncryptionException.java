package org.kopingenieria.exception;

public class EncryptionException extends Exception {
    public EncryptionException(String mensaje) {
        super(mensaje);
    }
    public EncryptionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
