package org.kopingenieria.audit.exceptions;

public class AuditException extends Exception{
    public AuditException(String mensaje) {
        super(mensaje);
    }
    public AuditException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
