package org.kopingenieria.exceptions;

public class MappingException extends AuditException {
    public MappingException(String mensaje) {
        super(mensaje);
    }
    public MappingException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
