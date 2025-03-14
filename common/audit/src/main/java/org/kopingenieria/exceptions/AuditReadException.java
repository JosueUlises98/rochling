package org.kopingenieria.exceptions;

public class AuditReadException extends AuditException {
    public AuditReadException(String mensaje) {
        super(mensaje);
    }
    public AuditReadException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
