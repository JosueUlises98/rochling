package org.kopingenieria.exceptions;

public class AuditPersistenceException extends AuditException {
    public AuditPersistenceException(String mensaje) {
        super(mensaje);
    }
    public AuditPersistenceException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
